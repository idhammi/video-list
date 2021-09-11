package id.idham.videolist.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.idham.videolist.ui.addpost.AddPostDialogFragment
import id.idham.videolist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabPost.setOnClickListener {
            AddPostDialogFragment().show(supportFragmentManager, AddPostDialogFragment.TAG)
        }
    }

}