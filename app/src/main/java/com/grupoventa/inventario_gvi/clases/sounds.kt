package com.grupoventa.inventario_gvi.clases

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import com.grupoventa.inventario_gvi.R
import es.dmoral.toasty.Toasty
import javax.inject.Inject

class sounds @Inject constructor(){

    fun PlaySoundSuccess(context: Context){
        val mediaPlayer = MediaPlayer.create(context, R.raw.beep)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()

        Toasty.success(context,"Item Registrado",Toasty.LENGTH_SHORT,true).show()
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
        Toasty.warning(context,"El artículo no se encontró en el inventario actual",Toasty.LENGTH_SHORT,true).show()
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