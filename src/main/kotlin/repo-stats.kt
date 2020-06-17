import khttp.get
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime

data class RepoStats(
    val watchers: Int, // watchers: Int => "watchers"
    val stars: Int, // stars: Int => "stargazers_count"
    val forks: Int, // forks: Int => "forks"
    val openIssues: Int, // issues, open: Int  => "open_issues"
    val openPRs: Int, // open prs: Int => [repo]/pulls (count the number of entries)
    val contributors: Int, // contributors: Int => [repo]/contributors (count the number of entries)
    val latestCommit: String  // last commit: timestamp "pushed_at"
)

fun getRepoStarsStats(repoURL: String): RepoStats {
    val pullsURL = "$repoURL/pulls"
    val pulls =  get(pullsURL).jsonArray.length()

    val contribURL = "$repoURL/contributors"
    val contributors =  get(contribURL).jsonArray.length()

    val resp = get(repoURL).jsonObject

    return RepoStats(
        resp["watchers"] as Int,
        resp["stargazers_count"] as Int,
        resp["forks"] as Int,
        resp["open_issues"] as Int,
        pulls,
        contributors,
        resp["pushed_at"] as String
    )
}

fun FileWriter.appendComma(c: Any) {append(c.toString()); append(",");}
fun writeStatsToCSV(csvPath: String, repostats: Map<String,RepoStats>) {
    val fileWriter: FileWriter? = null
    try {
        FileWriter(csvPath).run {
            for ((k,v) in repostats) {
                appendComma(k)
                appendComma(v.watchers)
                appendComma(v.stars)
                appendComma(v.forks)
                appendComma(v.openIssues)
                appendComma(v.openPRs)
                appendComma(v.contributors)
                appendComma(v.latestCommit)
                append(LocalDateTime.now().toString())
                append('\n')
            }
        }
        println("Write CSV successfully!")
    } catch (e: Exception) {
        println("Writing CSV error!")
        e.printStackTrace()
    } finally {
        try {
            fileWriter?.flush()
            fileWriter?.close()
        } catch (e: IOException) {
            println("Flushing/closing error!")
            e.printStackTrace()
        }
    }
}

fun main() {
    // reading the list of repos from a file
    val currentPath = "${System.getProperty("user.dir")}/src/main/kotlin/"
    val reposTextPath = "${currentPath}repos.txt"
    val repos = File(reposTextPath).readLines().toSet()

    // TODO: deal with authentication. already hit the limit. docs: https://developer.github.com/v3/#rate-limiting

    // getting the stats and writing them into a csv
    val records = repos.map { it to getRepoStarsStats("https://api.github.com/repos/$it") }.toMap()
    writeStatsToCSV("${currentPath}stats.csv", records)

}
