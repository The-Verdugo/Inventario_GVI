package com.grupoventa.inventario_gvi.clases

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import com.grupoventa.inventario_gvi.R

class sounds {

    fun PlaySoundSuccess(context: Context){
        val mediaPlayer = MediaPlayer.create(context, R.raw.beep)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }
    fun PlaySoundConfirm(context: Context){
        val mediaPlayer = MediaPlayer.create(context, R.raw.success)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }

    fun PlaySoundError(context: Context){
        val mediaPlayer = MediaPlayer.create(context, R.raw.beep_beep)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
        // Vibrar cuando se produce un error
        vibrate(context)
    }

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // Versiones anteriores a Oreo
                @Suppress("DEPRECATION")
                it.vibrate(400)
            }
        }
    }

}