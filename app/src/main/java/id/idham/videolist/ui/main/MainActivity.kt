package id.idham.videolist.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import id.idham.videolist.MyApp
import id.idham.videolist.databinding.ActivityMainBinding
import id.idham.videolist.ui.addpost.AddPostDialogFragment
import id.idham.videolist.ui.custom.NewItem
import id.idham.videolist.ui.custom.VideoRecyclerAdapter
import id.idham.videolist.ui.custom.VideoRecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvFeed: VideoRecyclerView
    private lateinit var adapter: VideoRecyclerAdapter

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((this.application as MyApp).database.itemUrlDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lytMain.lifecycleOwner = this
        binding.lytMain.viewModel = viewModel

        setContentView(binding.root)

        binding.fabPost.setOnClickListener {
            AddPostDialogFragment().show(supportFragmentManager, AddPostDialogFragment.TAG)
        }

        initVideo()

        viewModel.allItems.observe(this) { list ->
            viewModel.isItemsAvailable.value = list.isNotEmpty()
            list?.let { newList ->
                adapter.submitList(newList)
            }
        }
    }

    private fun initVideo() {
        rvFeed = binding.lytMain.rvFeed
        adapter = VideoRecyclerAdapter(object : NewItem {
            override fun onNewItemInsert(inserted: Boolean) {
                if (inserted) rvFeed.scrollToPosition(0)
            }

        })
        rvFeed.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (::rvFeed.isInitialized) rvFeed.createPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (::rvFeed.isInitialized) rvFeed.releasePlayer()
    }

}