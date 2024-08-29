package com.example.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.network.NetworkSchemeHandler
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle

class Recommendations : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recommendations)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val recommendationRequest = findViewById<Button>(R.id.request_recommendations)
        val recommendationText = findViewById<TextView>(R.id.recommendations_text)
        val recommendationSV = findViewById<ScrollView>(R.id.recommendations_scroll_view)
//        recommendationText.text = resources.getString(R.string.markdown_text)
        recommendationRequest.setOnClickListener {
            recommendationSV.visibility = View.VISIBLE
        }

        val prism4j = Prism4j(ExampleGrammarLocator())
        val syntaxHighlightPlugin = SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create())

        val imagesPlugin = ImagesPlugin.create { plugin ->
            plugin.addSchemeHandler(NetworkSchemeHandler.create())
        }
        // Configure Markwon
        val markwon = Markwon.builder(this)
            .usePlugin(imagesPlugin)
            .usePlugin(syntaxHighlightPlugin)
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .build()

        markwon.setMarkdown(recommendationText, resources.getString(R.string.another_markdown_text))

        val homepageButton = findViewById<FrameLayout>(R.id.home_button)
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        homepageButton.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
        }
        trackingButton.setOnClickListener {
            startActivity(Intent(this, Tracking::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
        }
    }
}

@PrismBundle(include = ["kotlin", "java"],
    grammarLocatorClassName = ".ExampleGrammarLocator")
class GrammarLocator { }