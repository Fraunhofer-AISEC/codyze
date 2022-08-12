plugins { id("kotlin") }

kotlin { isAwesome = true }

task("make an apple pie") {
    dependsOn("invent the universe")
    perform { println("🥧") }
}