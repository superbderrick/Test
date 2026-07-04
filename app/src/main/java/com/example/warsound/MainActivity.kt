package com.example.warsound

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import com.example.warsound.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Volatile
    private var activeMode: SoundMode? = null
    private val vibrator: Vibrator? by lazy { resolveVibrator() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.airRaidButton.setOnClickListener { playMode(SoundMode.AIR_RAID) }
        binding.radioButton.setOnClickListener { playMode(SoundMode.RADIO_CHATTER) }
        binding.alarmButton.setOnClickListener { playMode(SoundMode.BATTLE_ALARM) }
        binding.marchButton.setOnClickListener { playMode(SoundMode.MARCH_BEAT) }
        binding.morseButton.setOnClickListener { playMode(SoundMode.MORSE_SIGNAL) }
        binding.attentionButton.setOnClickListener { playMode(SoundMode.ATTENTION_CALL) }
        binding.holdButton.setOnClickListener { playMode(SoundMode.HOLD_POSITION) }
        binding.evacButton.setOnClickListener { playMode(SoundMode.EVAC_SIGNAL) }
        binding.stopButton.setOnClickListener { stopPlayback() }

        binding.statusText.text = getString(R.string.status_idle)
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    private fun playMode(mode: SoundMode) {
        stopPlayback()
        activeMode = mode
        binding.statusText.text = getString(R.string.status_active, getString(mode.labelRes))

        thread(name = "sound-mode-${mode.name.lowercase()}") {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            try {
                while (activeMode == mode) {
                    for (pulse in mode.pattern) {
                        if (activeMode != mode) {
                            break
                        }
                        tone.startTone(pulse.toneType, pulse.durationMs)
                        vibrate(pulse.durationMs.toLong(), pulse.vibrate)
                        Thread.sleep((pulse.durationMs + pulse.pauseAfterMs).toLong())
                    }
                }
            } finally {
                tone.release()
            }
        }
    }

    private fun stopPlayback() {
        activeMode = null
        vibrator?.cancel()
        binding.statusText.text = getString(R.string.status_idle)
    }

    private fun vibrate(durationMs: Long, enabled: Boolean) {
        if (!enabled) return
        val engine = vibrator ?: return
        val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        engine.vibrate(effect)
    }

    private fun resolveVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }
}

private enum class SoundMode(val labelRes: Int, val pattern: List<TonePulse>) {
    AIR_RAID(
        R.string.air_raid,
        listOf(
            TonePulse(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 900, 150, true),
            TonePulse(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 900, 250, true)
        )
    ),
    RADIO_CHATTER(
        R.string.radio_chatter,
        listOf(
            TonePulse(ToneGenerator.TONE_PROP_BEEP, 180, 60, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 140, 60, false),
            TonePulse(ToneGenerator.TONE_SUP_RADIO_ACK, 220, 120, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP, 100, 180, false)
        )
    ),
    BATTLE_ALARM(
        R.string.battle_alarm,
        listOf(
            TonePulse(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500, 90, true),
            TonePulse(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500, 160, true)
        )
    ),
    MARCH_BEAT(
        R.string.march_beat,
        listOf(
            TonePulse(ToneGenerator.TONE_DTMF_1, 160, 70, true),
            TonePulse(ToneGenerator.TONE_DTMF_1, 160, 70, false),
            TonePulse(ToneGenerator.TONE_DTMF_3, 220, 180, true)
        )
    ),
    MORSE_SIGNAL(
        R.string.morse_signal,
        listOf(
            TonePulse(ToneGenerator.TONE_PROP_BEEP, 120, 80, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP, 120, 80, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP, 120, 180, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 360, 120, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 360, 120, false)
        )
    ),
    ATTENTION_CALL(
        R.string.attention_call,
        listOf(
            TonePulse(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 420, 80, true),
            TonePulse(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 420, 80, true),
            TonePulse(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 680, 260, true)
        )
    ),
    HOLD_POSITION(
        R.string.hold_position,
        listOf(
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 220, 60, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 220, 60, false),
            TonePulse(ToneGenerator.TONE_PROP_BEEP2, 220, 220, true),
            TonePulse(ToneGenerator.TONE_SUP_PIP, 500, 240, false)
        )
    ),
    EVAC_SIGNAL(
        R.string.evac_signal,
        listOf(
            TonePulse(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 700, 80, true),
            TonePulse(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 700, 80, true),
            TonePulse(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 700, 300, true)
        )
    );
}

private data class TonePulse(
    val toneType: Int,
    val durationMs: Int,
    val pauseAfterMs: Int,
    val vibrate: Boolean
)
