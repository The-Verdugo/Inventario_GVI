package com.grupoventa.inventario_gvi.views.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grupoventa.inventario_gvi.R
import com.grupoventa.inventario_gvi.clases.FormatText
import com.grupoventa.inventario_gvi.clases.Validaciones
import com.grupoventa.inventario_gvi.clases.sounds
import com.grupoventa.inventario_gvi.views.ui.MainActivity
import es.dmoral.toasty.Toasty
import java.lang.reflect.Type
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Fragment_home : Fragment() {
    private lateinit var ScannInput: EditText
    private lateinit var ItemCode:EditText
    private lateinit var ItemName:EditText
    private lateinit var SerLot: EditText
    private lateinit var Cantidad:EditText
    private lateinit var ButtonOK:Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var ButtonCancel:Button
    private lateinit var txtCantUbi:TextView
    private lateinit var txtCantidadTotal:TextView
    private lateinit var btnResetUbi:ImageButton
    private var SoundPlayer: sounds = sounds()
    private var Format: FormatText = FormatText()
    private var Validar:Validaciones = Validaciones()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        ScannInput = rootView.findViewById(R.id.ScannText)
        ItemCode = rootView.findViewById(R.id.editProductCode)
        ItemName = rootView.findViewById(R.id.editSKU)
        Cantidad = rootView.findViewById(R.id.editCantidad)
        SerLot = rootView.findViewById(R.id.editSer_Lot)
        ButtonOK = rootView.findViewById(R.id.btnConfirmQTY)
        ButtonCancel = rootView.findViewById(R.id.btnCancelQTY)
        txtCantUbi = rootView.findViewById(R.id.txtCantidadUbi)
        txtCantidadTotal = rootView.findViewById(R.id.txtCantidadTotal)
        btnResetUbi = rootView.findViewById(R.id.btnResetUbi)
        mAuth = FirebaseAuth.getInstance()

        (activity as MainActivity).setActionBarTitle("Inicio")

        ScannInput.apply {
            // Solicitar el enfoque inicial en el campo de texto
            requestFocus()
            // Configurar el InputType para evitar que se muestre el teclado
            inputType = InputType.TYPE_NULL
            // Configurar el KeyListener para detectar la tecla "Enter"
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Obtener el texto del campo de texto
                    val qrText = ScannInput.text.toString()
                    var itemCode: String
                    var loteFormat: String
                    val lote: String

                    if (qrText.contains("|")) {
                        itemCode = qrText.substringBefore('|')
                        loteFormat = qrText.substringAfter('|')

                        // Remover caracteres no deseados y conservar solo los caracteres normales
                        loteFormat = Format.removeSpecialCharsAndAccents(loteFormat).trim()
                        lote = if (loteFormat.contains('@')) {
                            loteFormat.substringBefore('@').trim()
                        } else {
                            loteFormat
                        }

                        // Actualizar el campo de cantidad leída para el item, si es que se
                        Validar.ValidarExistenciaLot(itemCode,lote) { existe, data ->
                            if (existe) {
                                // La serie existe, y tu Objeto contiene la información recuperada
                                ItemCode.setText(data?.ItemCode)
                                ItemName.setText(data?.itemName)
                                SerLot.setText(data?.DistNumber)

                                if (data?.Tipo_Conteo == 1) {
                                    // Cambiar el foco al EditText llamado Cantidad
                                    postDelayed({
                                        // Borrar el texto después de un breve retraso
                                        ScannInput.text.clear()

                                        // Obtener el valor de Tipo_Conteo
                                        val tipoConteo = data?.Tipo_Conteo ?: 0

                                        if (tipoConteo == 1) {
                                            Cantidad.isEnabled = true
                                            Cantidad.inputType = InputType.TYPE_CLASS_NUMBER
                                            Cantidad.isFocusable = true
                                            Cantidad.isFocusableInTouchMode = true

                                            ScannInput.isEnabled = false
                                            ScannInput.isFocusable = false
                                            ScannInput.isFocusableInTouchMode = false
                                            ScannInput.inputType = InputType.TYPE_NULL

                                            ButtonCancel.visibility = View.VISIBLE
                                            ButtonCancel.isEnabled = true
                                            ButtonOK.visibility = View.VISIBLE
                                            ButtonOK.isEnabled = true

                                            Cantidad.setText("1")

                                            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                            // Cambiar el foco al campo Cantidad después de un breve retraso
                                            Cantidad.requestFocus()
                                            Cantidad.selectAll()
                                            Cantidad.setHighlightColor(ContextCompat.getColor(
                                                context, R.color.pbSecuntario));
                                            // Mostrar el teclado virtual
                                            imm.showSoftInput(Cantidad, InputMethodManager.SHOW_IMPLICIT)

                                            SoundPlayer.PlaySoundSuccess(requireContext())
                                            // Agregar un listener para prevenir que el usuario salga hasta que ingrese una cantidad mayor a 0
                                            Cantidad.setOnFocusChangeListener { _, hasFocus ->
                                                if (!hasFocus) {
                                                    // El usuario intenta salir del EditText
                                                    val cantidadIngresada =
                                                        Cantidad.text.toString().toIntOrNull() ?: 0
                                                    if (cantidadIngresada <= 0) {
                                                        // Mostrar un mensaje o realizar alguna acción para indicar que la cantidad debe ser mayor a 0
                                                        Cantidad.error =
                                                            "La cantidad debe ser mayor a 0"
                                                        Cantidad.requestFocus() // Vuelve a enfocar para que corrija la cantidad
                                                    }
                                                }
                                            }
                                            Cantidad.addTextChangedListener(object : TextWatcher {
                                                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                                                    // Este método se llama para notificar que los caracteres dentro de charSequence cambiarán.
                                                    // No necesitas realizar ninguna acción específica antes del cambio.
                                                }

                                                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                                                    // Este método se llama para notificar que los caracteres dentro de charSequence han cambiado.
                                                    // Puedes realizar acciones aquí basadas en el texto actual.

                                                    val textoIngresado = charSequence?.toString() ?: ""

                                                    // Verificar si el texto no está vacío
                                                    if (textoIngresado.isNotEmpty()) {
                                                        // Aquí puedes realizar acciones cuando el campo no está vacío
                                                        ButtonOK.visibility = View.VISIBLE
                                                        ButtonOK.isEnabled = true
                                                    } else {
                                                        // Aquí puedes realizar acciones cuando el campo está vacío
                                                        ButtonOK.visibility = View.GONE
                                                        ButtonOK.isEnabled = false
                                                    }
                                                }

                                                override fun afterTextChanged(editable: Editable?) {
                                                    // Este método se llama para notificar que los caracteres dentro de editable han cambiado.
                                                    // No necesitas realizar ninguna acción específica después del cambio.
                                                }
                                            })
                                        } else {
                                            // Si Tipo_Conteo no es igual a 1, mantener el foco en ScannInput
                                            Cantidad.setText("1");
                                            ScannInput.isEnabled = true
                                            ScannInput.isFocusable = true
                                            ScannInput.isFocusableInTouchMode = true
                                            ScannInput.inputType = InputType.TYPE_NULL
                                            SoundPlayer.PlaySoundSuccess(requireContext())
                                            ScannInput.requestFocus()
                                        }
                                    }, 100)
                                }
                            } else {
                                // La serie no existe o hubo un error
                                Toasty.warning(requireContext(),"El SKU/Lote no existe en el inventario",Toasty.LENGTH_SHORT,true).show()
                                SoundPlayer.PlaySoundError(requireContext())
                            }
                        }
                    }else if (qrText.count { it == '-' } == 2 && Regex("[0-9a-zA-Z]-[0-9a-zA-Z]{2}-[0-9a-zA-Z]").matches(qrText)) {
                        SoundPlayer.PlaySoundConfirm(context)
                        mostrarConfirmacionAsignarUbicacion(mAuth.currentUser?.uid.toString(), qrText)
                        ScannInput.isEnabled = true
                        ScannInput.isFocusable = true
                        ScannInput.isFocusableInTouchMode = true
                        ScannInput.inputType = InputType.TYPE_NULL
                        postDelayed({
                            ScannInput.text.clear()
                            ScannInput.requestFocus()
                        },100)
                    } else {
                        // No se encontró el carácter "|", realizar otras acciones
                        var serie = ""
                        itemCode = ""
                        lote = ""
                        when (qrText.length) {
                            42 -> {
                                // Si la longitud es de 42 caracteres, extraer los últimos 8 caracteres y convertir a mayúsculas
                                serie = qrText.takeLast(8).uppercase()
                            }
                            44 -> {
                                // Si la longitud es de 44 caracteres, extraer los últimos 10 caracteres y convertir a mayúsculas
                                serie = qrText.takeLast(10).uppercase()
                            }
                            43 -> {
                                // Si la longitud es de 43 caracteres, extraer los últimos 9 caracteres y convertir a mayúsculas
                                serie = qrText.takeLast(9).uppercase()
                            }
                            45 -> {
                                // Si la longitud es de 45 caracteres, extraer los últimos 10 caracteres y convertir a mayúsculas
                                serie = qrText.takeLast(10).uppercase()
                            }
                            else -> {
                                itemCode = qrText
                            }
                        }
                        if (serie.isNotEmpty()){
                        Validar.ValidarExistenciaIM(serie) { existe, data ->
                            if (existe) {
                                // La serie existe, y tu Objeto contiene la información recuperada
                                ItemCode.setText(data?.ItemCode)
                                ItemName.setText(data?.itemName)
                                SerLot.setText(data?.DistNumber)

                                if (data?.Tipo_Conteo == 1) {
                                    // Cambiar el foco al EditText llamado Cantidad
                                    Cantidad.requestFocus()

                                    // Habilitar el EditText
                                    Cantidad.isEnabled = true

                                    // Agregar un listener para prevenir que el usuario salga hasta que ingrese una cantidad mayor a 0
                                    Cantidad.setOnFocusChangeListener { _, hasFocus ->
                                        if (!hasFocus) {
                                            // El usuario intenta salir del EditText
                                            val cantidadIngresada = Cantidad.text.toString().toIntOrNull() ?: 0
                                            if (cantidadIngresada <= 0) {
                                                // Mostrar un mensaje o realizar alguna acción para indicar que la cantidad debe ser mayor a 0
                                                Cantidad.error = "La cantidad debe ser mayor a 0"
                                                Cantidad.requestFocus() // Vuelve a enfocar para que corrija la cantidad
                                            }
                                        }
                                    }
                                }else{
                                    Cantidad.setText("1");
                                    ButtonOK.visibility = View.GONE
                                    ButtonCancel.visibility = View.GONE
                                    mAuth.currentUser?.let {
                                        Validar.registrarConteoSerie(
                                            Cantidad.text.toString().toIntOrNull() ?: 1,
                                            data!!,
                                            it.uid
                                        ) { exitoso, mensaje ->
                                            if (exitoso) {
                                                // Operación exitosa, muestra un Toast con un mensaje corto
                                                Toasty.success(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                SoundPlayer.PlaySoundSuccess(requireContext())
                                            } else {
                                                // Error al registrar, muestra un Toast con un mensaje de error
                                                Toasty.error(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                Cantidad.setText("0");
                                                SoundPlayer.PlaySoundError(requireContext())
                                            }
                                        }
                                    }

                                }
                            } else{
                                // La serie no existe o hubo un error
                                Toasty.warning(requireContext(),"La Serie/Código no existe en el inventario",Toasty.LENGTH_SHORT,true).show()
                                SoundPlayer.PlaySoundError(requireContext())
                            }
                        }
                    }else if (itemCode.isNotEmpty()) {
                            Validar.ValidarExistenciaSKU(itemCode) { existe, data ->
                                if (existe) {
                                    // La serie existe, y tu Objeto contiene la información recuperada
                                    ItemCode.setText(data?.ItemCode)
                                    ItemName.setText(data?.itemName)
                                    SerLot.setText(data?.DistNumber)

                                    if (data?.Tipo_Conteo == 1) {
                                        // Cambiar el foco al EditText llamado Cantidad
                                        postDelayed({
                                            // Borrar el texto después de un breve retraso
                                            ScannInput.text.clear()

                                                Cantidad.isEnabled = true
                                                Cantidad.inputType = InputType.TYPE_CLASS_NUMBER
                                                Cantidad.isFocusable = true
                                                Cantidad.isFocusableInTouchMode = true

                                                ScannInput.isEnabled = false
                                                ScannInput.isFocusable = false
                                                ScannInput.isFocusableInTouchMode = false
                                                ScannInput.inputType = InputType.TYPE_NULL

                                                ButtonCancel.visibility = View.VISIBLE
                                                ButtonCancel.isEnabled = true
                                                ButtonOK.visibility = View.VISIBLE
                                                ButtonOK.isEnabled = true

                                                Cantidad.setText("1")

                                                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                // Cambiar el foco al campo Cantidad después de un breve retraso
                                                Cantidad.requestFocus()
                                                Cantidad.selectAll()
                                                Cantidad.setHighlightColor(ContextCompat.getColor(
                                                    context, R.color.pbSecuntario));
                                                // Mostrar el teclado virtual
                                                imm.showSoftInput(Cantidad, InputMethodManager.SHOW_IMPLICIT)

                                                SoundPlayer.PlaySoundSuccess(requireContext())
                                                // Agregar un listener para prevenir que el usuario salga hasta que ingrese una cantidad mayor a 0
                                                Cantidad.setOnFocusChangeListener { _, hasFocus ->
                                                    if (!hasFocus) {
                                                        // El usuario intenta salir del EditText
                                                        val cantidadIngresada =
                                                            Cantidad.text.toString().toIntOrNull() ?: 0
                                                        if (cantidadIngresada <= 0) {
                                                            // Mostrar un mensaje o realizar alguna acción para indicar que la cantidad debe ser mayor a 0
                                                            Cantidad.error =
                                                                "La cantidad debe ser mayor a 0"
                                                            Cantidad.requestFocus() // Vuelve a enfocar para que corrija la cantidad
                                                        }
                                                    }
                                                }
                                                Cantidad.addTextChangedListener(object : TextWatcher {
                                                    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                                                        // Este método se llama para notificar que los caracteres dentro de charSequence cambiarán.
                                                        // No necesitas realizar ninguna acción específica antes del cambio.
                                                    }

                                                    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                                                        // Este método se llama para notificar que los caracteres dentro de charSequence han cambiado.
                                                        // Puedes realizar acciones aquí basadas en el texto actual.

                                                        val textoIngresado = charSequence?.toString() ?: ""

                                                        // Verificar si el texto no está vacío
                                                        if (textoIngresado.isNotEmpty()) {
                                                            // Aquí puedes realizar acciones cuando el campo no está vacío
                                                            ButtonOK.visibility = View.VISIBLE
                                                            ButtonOK.isEnabled = true
                                                        } else {
                                                            // Aquí puedes realizar acciones cuando el campo está vacío
                                                            ButtonOK.visibility = View.GONE
                                                            ButtonOK.isEnabled = false
                                                        }
                                                    }

                                                    override fun afterTextChanged(editable: Editable?) {
                                                        // Este método se llama para notificar que los caracteres dentro de editable han cambiado.
                                                        // No necesitas realizar ninguna acción específica después del cambio.
                                                    }
                                                })
                                                ButtonCancel.setOnClickListener{
                                                    ScannInput.isEnabled = true
                                                    ScannInput.isFocusable = true
                                                    ScannInput.isFocusableInTouchMode = true
                                                    ScannInput.inputType = InputType.TYPE_NULL
                                                    Cantidad.isEnabled = false
                                                    Cantidad.inputType = InputType.TYPE_NULL
                                                    Cantidad.isFocusable = false
                                                    Cantidad.isFocusableInTouchMode = true
                                                    CleanForm()
                                                    ButtonCancel.visibility = View.GONE
                                                    postDelayed({
                                                        ScannInput.requestFocus()
                                                    },100)
                                                }
                                            ButtonOK.setOnClickListener{
                                                ScannInput.isEnabled = true
                                                ScannInput.isFocusable = true
                                                ScannInput.isFocusableInTouchMode = true
                                                ScannInput.inputType = InputType.TYPE_NULL
                                                Cantidad.isEnabled = false
                                                Cantidad.inputType = InputType.TYPE_NULL
                                                Cantidad.isFocusable = false
                                                Cantidad.isFocusableInTouchMode = true
                                                ButtonCancel.visibility = View.GONE
                                                mAuth.currentUser?.let {
                                                    Validar.registrarConteoSKU(
                                                        Cantidad.text.toString().toIntOrNull() ?: 1,
                                                        data!!,
                                                        it.uid
                                                    ) { exitoso, mensaje ->
                                                        if (exitoso) {
                                                            // Operación exitosa, muestra un Toast con un mensaje corto
                                                            Toasty.success(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                            SoundPlayer.PlaySoundSuccess(requireContext())
                                                            CleanForm()
                                                        } else {
                                                            // Error al registrar, muestra un Toast con un mensaje de error
                                                            Toasty.error(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                            Cantidad.setText("0");
                                                            SoundPlayer.PlaySoundError(requireContext())
                                                        }
                                                    }
                                                }
                                                postDelayed({
                                                    ScannInput.requestFocus()
                                                },100)
                                            }
                                        }, 100)
                                    }else{
                                        Cantidad.setText("1");
                                        ButtonOK.visibility = View.GONE
                                        ButtonCancel.visibility = View.GONE
                                        mAuth.currentUser?.let {
                                            Validar.registrarConteoSKU(
                                                Cantidad.text.toString().toIntOrNull() ?: 1,
                                                data!!,
                                                it.uid
                                            ) { exitoso, mensaje ->
                                                if (exitoso) {
                                                    // Operación exitosa, muestra un Toast con un mensaje corto
                                                    Toasty.success(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                    SoundPlayer.PlaySoundSuccess(requireContext())
                                                } else {
                                                    // Error al registrar, muestra un Toast con un mensaje de error
                                                    Toasty.error(context, mensaje, Toasty.LENGTH_SHORT).show()
                                                    Cantidad.setText("0");
                                                    SoundPlayer.PlaySoundError(requireContext())
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // La serie no existe o hubo un error
                                    Toasty.warning(requireContext(),"El SKU/Lote no existe en el inventario",Toasty.LENGTH_SHORT,true).show()
                                    SoundPlayer.PlaySoundError(requireContext())
                                }
                            }
                        }
                        ScannInput.text.clear()
                    }

                    return@setOnKeyListener true
                } else {
                    return@setOnKeyListener false
                }
                false
            }
        }

        // Crea la referencia al nodo correspondiente
        val conteoNumRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Inventarios/${Validar.obtenerFechaActual()}/conteo/num_conteo")

        // Agrega el listener para obtener el valor una vez
        conteoNumRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si el nodo existe y tiene un valor
                if (snapshot.exists()) {
                    // Obtiene el valor de num_conteo
                    val num_conteo = snapshot.getValue(Int::class.java)

                    // Crea la referencia al nodo conteo correspondiente
                    val conteoRef: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Inventarios/${Validar.obtenerFechaActual()}/conteo/$num_conteo")

                    // Agrega el listener para obtener los nodos correspondientes
                    conteoRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Verifica si el nodo existe y tiene hijos
                            if (snapshot.exists() && snapshot.hasChildren()) {
                                // Filtra los nodos según tus criterios
                                val nodosFiltrados = snapshot.children.filter { child ->
                                    // Verifica si el UID del usuario actual coincide y la Ubicacion está vacía
                                    child.child("idUser").getValue(String::class.java) == mAuth.currentUser?.uid &&
                                            child.child("Ubicacion").getValue(String::class.java) == ""
                                }
                                val nodosFiltradosTotal = snapshot.children.filter { child ->
                                    // Verifica si el UID del usuario actual coincide y la Ubicacion está vacía
                                    child.child("idUser").getValue(String::class.java) == mAuth.currentUser?.uid &&
                                            child.child("Ubicacion").getValue(String::class.java) != ""
                                }

                                // Calcula la suma de las cantidades solo para los nodos que cumplen las condiciones
                                val sumaCantidades = nodosFiltrados.sumBy { child ->
                                    child.child("cantidad").getValue(Int::class.java) ?: 0
                                }
                                val sumaTotal = nodosFiltradosTotal.sumBy { child ->
                                    child.child("cantidad").getValue(Int::class.java) ?: 0
                                }


                                // Ahora puedes usar la variable sumaCantidades según tus necesidades
                                txtCantUbi.text = "Piezas Contabilizadas: $sumaCantidades"
                                txtCantidadTotal.text = "Piezas Totales: $sumaTotal"
                            } else {
                                // El nodo no existe o no tiene hijos
                                txtCantUbi.text = "Piezas Contabilizadas: 0"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Maneja errores si es necesario
                            println("Error al obtener datos: ${error.message}")
                        }
                    })
                } else {
                    // El nodo no existe o no tiene un valor
                    println("El nodo num_conteo no existe o no tiene un valor.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja errores si es necesario
                println("Error al obtener datos: ${error.message}")
            }
        })

        btnResetUbi.setOnClickListener{
            mostrarConfirmacionReinicio(mAuth.currentUser?.uid.toString())
        }

        return rootView;
    }

    fun CleanForm(){
        ItemCode.setText("");
        ItemName.setText("");
        SerLot.setText("");
        Cantidad.setText("");
    }
    fun mostrarConfirmacionAsignarUbicacion(IdUser: String, IdUbi: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar ubicación: $IdUbi")
        builder.setMessage("Se asignará la ubicación $IdUbi a los elementos contabilizados. ¿Desea continuar?")

        builder.setPositiveButton("Confirmar") { _, _ ->
            // Usuario hizo clic en "Confirmar", llama a la función asignarUbicacion
            Validar.asignarUbicacion(IdUser, IdUbi) { success, message ->
                if (success) {
                    // La asignación fue exitosa, puedes realizar acciones adicionales si es necesario
                    Toasty.success(requireContext(),  "$message", Toasty.LENGTH_SHORT).show()
                    CleanForm()
                    ScannInput.requestFocus()
                } else {
                    // Hubo un error en la asignación, maneja el error según sea necesario
                    // Puedes mostrar un mensaje de error o realizar otras acciones
                    Toasty.error(requireContext(), "Error: $message", Toasty.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Cancelar") { _, _ ->
            // Usuario hizo clic en "Cancelar", no se hace nada
        }

        builder.show()

    }

    fun mostrarConfirmacionReinicio(IdUser: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reiniciar conteo")
        builder.setMessage("Se reiniciará el conteo para la ubicación actual. ¿Desea continuar?")

        builder.setPositiveButton("Confirmar") { _, _ ->
            // Usuario hizo clic en "Confirmar", llama a la función asignarUbicacion
            Validar.resetUbi(IdUser) { success, message ->
                if (success) {
                    // La asignación fue exitosa, puedes realizar acciones adicionales si es necesario
                    Toasty.success(requireContext(),  "$message", Toasty.LENGTH_SHORT).show()
                    CleanForm()
                    ScannInput.requestFocus()
                } else {
                    // Hubo un error en la asignación, maneja el error según sea necesario
                    // Puedes mostrar un mensaje de error o realizar otras acciones
                    Toasty.error(requireContext(), "Error: $message", Toasty.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Cancelar") { _, _ ->
            // Usuario hizo clic en "Cancelar", no se hace nada
        }

        builder.show()

    }


}