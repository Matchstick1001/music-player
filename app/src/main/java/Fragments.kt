package com.example.fzo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels { AudioViewModelFactory(requireContext()) }
    private val settingsViewModel: SettingsViewModel by activityViewModels { SettingsViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_songs)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = SongAdapter { song ->
            audioViewModel.playSong(song)
        }
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.playlistFlow.collectLatest { songs ->
                adapter.submitList(songs)
            }
        }

        // Mini player binding
        val miniCover = view.findViewById<ImageView>(R.id.image_mini_cover)
        val miniTitle = view.findViewById<TextView>(R.id.text_mini_title)
        val miniArtist = view.findViewById<TextView>(R.id.text_mini_artist)
        val miniPlayPause = view.findViewById<ImageButton>(R.id.btn_mini_play_pause)

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    miniTitle.text = song.title
                    miniArtist.text = song.artist
                    if (song.coverUrl.isNotEmpty()) {
                        Glide.with(miniCover)
                            .load(song.coverUrl)
                            .placeholder(R.drawable.ic_music_note)
                            .error(R.drawable.ic_music_note)
                            .into(miniCover)
                    } else {
                        miniCover.setImageResource(R.drawable.ic_music_note)
                    }
                    view.findViewById<View>(R.id.mini_player).visibility = View.VISIBLE
                } else {
                    view.findViewById<View>(R.id.mini_player).visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.isPlaying.collectLatest { playing ->
                miniPlayPause.setImageResource(
                    if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
            }
        }

        miniPlayPause.setOnClickListener {
            audioViewModel.togglePlayPause()
        }

        view.findViewById<View>(R.id.mini_player).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, PlayerFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}

class PlayerFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels { AudioViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cover = view.findViewById<ImageView>(R.id.image_cover)
        val title = view.findViewById<TextView>(R.id.text_title)
        val artist = view.findViewById<TextView>(R.id.text_artist)
        val seekBar = view.findViewById<SeekBar>(R.id.seek_bar)
        val btnPrev = view.findViewById<ImageButton>(R.id.btn_prev)
        val btnPlayPause = view.findViewById<ImageButton>(R.id.btn_play_pause)
        val btnNext = view.findViewById<ImageButton>(R.id.btn_next)

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    title.text = song.title
                    artist.text = song.artist
                    if (song.coverUrl.isNotEmpty()) {
                        Glide.with(cover)
                            .load(song.coverUrl)
                            .placeholder(R.drawable.ic_music_note)
                            .error(R.drawable.ic_music_note)
                            .into(cover)
                    } else {
                        cover.setImageResource(R.drawable.ic_music_note)
                    }
                }
            }
        }

        val textCurrentTime = view.findViewById<TextView>(R.id.text_current_time)
        val textTotalTime = view.findViewById<TextView>(R.id.text_total_time)

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.durationMs.collectLatest { duration ->
                seekBar.max = duration.toInt().coerceAtLeast(1)
                textTotalTime.text = formatTime(duration)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.positionMs.collectLatest { pos ->
                seekBar.progress = pos.toInt().coerceAtLeast(0)
                textCurrentTime.text = formatTime(pos)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            audioViewModel.isPlaying.collectLatest { playing ->
                btnPlayPause.setImageResource(
                    if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioViewModel.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPrev.setOnClickListener { audioViewModel.previous() }
        btnPlayPause.setOnClickListener { audioViewModel.togglePlayPause() }
        btnNext.setOnClickListener { audioViewModel.next() }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by activityViewModels { SettingsViewModelFactory(requireContext()) }
    private val audioViewModel: AudioViewModel by activityViewModels { AudioViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchAuto = view.findViewById<Switch>(R.id.switch_auto_play)
        val switchLoop = view.findViewById<Switch>(R.id.switch_loop)
        val seekVolume = view.findViewById<SeekBar>(R.id.seek_volume)

        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.settings.collectLatest { settings ->
                switchAuto.isChecked = settings.autoPlayAll
                switchLoop.isChecked = settings.shuffleEnabled
                seekVolume.progress = (settings.volume * 100).toInt()
            }
        }

        switchAuto.text = "Auto Play All Songs"
        switchLoop.text = "Shuffle Mode"

        switchAuto.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setAutoPlayAll(isChecked)
            audioViewModel.applyAutoPlayAll(isChecked)
        }

        switchLoop.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setShuffleEnabled(isChecked)
            audioViewModel.applyShuffle(isChecked)
        }

        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val vol = progress / 100f
                    settingsViewModel.setVolume(vol)
                    audioViewModel.applyVolume(vol)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}

// --- RecyclerView adapter for songs ---

class SongAdapter(private val onClick: (Song) -> Unit) : RecyclerView.Adapter<SongViewHolder>() {
    private var songs: List<Song> = emptyList()

    fun submitList(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size
}

class SongViewHolder(itemView: View, private val onClick: (Song) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.text_song_title)
    private val artist: TextView = itemView.findViewById(R.id.text_song_artist)
    private var current: Song? = null

    init {
        itemView.setOnClickListener {
            current?.let(onClick)
        }
    }

    fun bind(song: Song) {
        current = song
        title.text = song.title
        artist.text = song.artist
    }
}
