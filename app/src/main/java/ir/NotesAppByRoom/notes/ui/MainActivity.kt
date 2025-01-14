package ir.NotesAppByRoom.notes.ui


import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        fragmentMain()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    val currentFragment =
                        supportFragmentManager.findFragmentById(R.id.containerView)

                    if (currentFragment is MainFragment) {

                        finish()

                    } else {

                        fragmentMain()

                    }
                }
            }
        )

    }

    private fun fragmentMain() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.containerView, MainFragment())
            .commit()

    }

}