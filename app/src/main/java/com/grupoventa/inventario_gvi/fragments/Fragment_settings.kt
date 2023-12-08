package com.grupoventa.inventario_gvi.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.grupoventa.inventario_gvi.R
import com.grupoventa.inventario_gvi.ui.view.Login


class Fragment_settings : Fragment() {
    private lateinit var btnLogout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        btnLogout = rootView.findViewById(R.id.btnLogout)

        sharedPreferences = requireContext().getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE)

        btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Borrar el valor de SharedPreferences
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Ir a la pantalla de inicio de sesión
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return rootView
    }
}