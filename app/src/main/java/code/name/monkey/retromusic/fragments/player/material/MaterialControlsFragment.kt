package code.name.monkey.retromusic.fragments.player.material

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.extensions.hide
import code.name.monkey.retromusic.extensions.ripAlpha
import code.name.monkey.retromusic.extensions.show
import code.name.monkey.retromusic.extensions.textColorSecondary
import code.name.monkey.retromusic.fragments.base.AbsPlayerControlsFragment
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper
import code.name.monkey.retromusic.helper.PlayPauseButtonOnClickHandler
import code.name.monkey.retromusic.misc.SimpleOnSeekbarChangeListener
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.ViewUtil
import kotlinx.android.synthetic.main.fragment_material_playback_controls.nextButton
import kotlinx.android.synthetic.main.fragment_material_playback_controls.playPauseButton
import kotlinx.android.synthetic.main.fragment_material_playback_controls.previousButton
import kotlinx.android.synthetic.main.fragment_material_playback_controls.progressSlider
import kotlinx.android.synthetic.main.fragment_material_playback_controls.repeatButton
import kotlinx.android.synthetic.main.fragment_material_playback_controls.shuffleButton
import kotlinx.android.synthetic.main.fragment_material_playback_controls.songCurrentProgress
import kotlinx.android.synthetic.main.fragment_material_playback_controls.songInfo
import kotlinx.android.synthetic.main.fragment_material_playback_controls.songTotalTime
import kotlinx.android.synthetic.main.fragment_material_playback_controls.text
import kotlinx.android.synthetic.main.fragment_material_playback_controls.title

/**
 * @author Hemanth S (h4h13).
 */
class MaterialControlsFragment : AbsPlayerControlsFragment() {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_material_playback_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMusicControllers()
        title.isSelected = true
        text.isSelected = true
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        title.text = song.title
        text.text = song.artistName

        if (PreferenceUtil.getInstance(requireContext()).isSongInfo) {
            songInfo.text = getSongInfo(song)
            songInfo.show()
        } else {
            songInfo.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setDark(color: Int) {
        val colorBg = ATHUtil.resolveColor(requireContext(), R.attr.colorSurface)
        if (ColorUtil.isColorLight(colorBg)) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
        }

        updateRepeatState()
        updateShuffleState()

        val colorFinal = if (PreferenceUtil.getInstance(requireContext()).adaptiveColor) {
            color
        } else {
            textColorSecondary(requireContext())
        }.ripAlpha()

        text.setTextColor(colorFinal)
        ViewUtil.setProgressDrawable(progressSlider, colorFinal, true)

        volumeFragment?.setTintable(colorFinal)

        updatePlayPauseColor(colorFinal)
        updatePrevNextColor(colorFinal)
    }

    private fun updatePlayPauseColor(color: Int) {
        playPauseButton.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpPlayPauseFab() {
        playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause_white_64dp)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_64dp)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor(textColorSecondary(requireContext()))
        nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor(color: Int) {
        nextButton.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        previousButton.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> shuffleButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setUpRepeatButton() {
        repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    override fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_ALL -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_THIS -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    public override fun show() {
    }

    public override fun hide() {
    }

    override fun setUpProgressSlider() {
        progressSlider.setOnSeekBarChangeListener(object : SimpleOnSeekbarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress)
                    onUpdateProgressViews(
                        MusicPlayerRemote.songProgressMillis,
                        MusicPlayerRemote.songDurationMillis
                    )
                }
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        progressSlider!!.max = total

        val animator = ObjectAnimator.ofInt(progressSlider, "progress", progress)
        animator.duration = SLIDER_ANIMATION_TIME
        animator.interpolator = LinearInterpolator()
        animator.start()

        songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
