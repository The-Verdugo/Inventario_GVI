package com.grupoventa.inventario_gvi.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.grupoventa.inventario_gvi.R
import com.grupoventa.inventario_gvi.clases.FormatText
import com.grupoventa.inventario_gvi.clases.Validaciones
import com.grupoventa.inventario_gvi.clases.sounds
import com.grupoventa.inventario_gvi.databinding.FragmentHomeBinding
import com.grupoventa.inventario_gvi.ui.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Fragment_home : Fragment() {
    private var SoundPlayer: sounds = sounds()
    private var Format: FormatText = FormatText()
    private var loadingDialog: Dialog? = null
    private var _binding: FragmentHomeBinding? = null
    private val ItemViewModel: ItemViewModel by viewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater,container,false)

        binding.ScannText.inputType = InputType.TYPE_NULL
        binding.ScannText.requestFocus()
        // Configura el listener para el evento "Enter" en el teclado
        binding.ScannText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                var itemCode = binding.ScannText.text.toString()

                Format.returnType(itemCode) { result, termFormatted ->
                    val sku = if (result == "SkuAndLot") termFormatted else itemCode
                    val lote = if (result == "SkuAndLot") itemCode.substringAfter("|") else ""
                    ItemViewModel.FindItemSKULOT(sku, Format.removeSpecialCharsAndAccents(lote), result)
                }

                binding.ScannText.text.clear()
                binding.ScannText.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        ItemViewModel.onCreate()
        ItemViewModel.inventarioModel.observe(viewLifecycleOwner, Observer {

        })
        ItemViewModel.Item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.editProductCode.setText(it.itemName)
                binding.editSKU.setText(it.ItemCode)
                binding.editSerLot.setText(it.DistNumber)
                binding.editCantidad.setText("1")
                SoundPlayer.PlaySoundSuccess(requireContext())
            }
            else{
                binding.editProductCode.text.clear()
                binding.editSKU.text.clear()
                binding.editSerLot.text.clear()
                binding.editCantidad.text.clear()
                SoundPlayer.PlaySoundError(requireContext())
            }

        })
        ItemViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it){
                showLoadingDialog()
            }else{
                hideLoadingDialog()
            }
        })
        return binding.root;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar la referencia al objeto de View Binding para evitar fugas de memoria
        _binding = null
    }

    private fun showLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog?.setContentView(R.layout.dialog_loading)
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }



//    private fun saveDataToSharedPreferences(sumaCantidades: Int, sumaTotal: Int, newVal:Int = 0) {
//        // Obtén el SharedPreferences del fragmento
//        val sharedPreferences = requireContext().getSharedPreferences("NombrePref", Context.MODE_PRIVATE)
//
//        // Usa un editor para realizar cambios en SharedPreferences
//        val editor = sharedPreferences.edit()
//
//        // Guarda los valores en SharedPreferences
//        editor.putInt("sumaCantidades", sumaCantidades)
//        editor.putInt("sumaTotal", sumaTotal)
//
//        // Aplica los cambios
//        editor.apply()
//    }
//
//    private fun updateViews(sumaCantidades: Int, sumaTotal: Int) {
//        // Actualiza las vistas con los resultados
//        txtCantUbi.text = "Piezas Contabilizadas: $sumaCantidades"
//        txtCantidadTotal.text = "Piezas Totales: $sumaTotal"
//    }
//
//    fun CleanForm(){
//        ItemCode.setText("");
//        ItemName.setText("");
//        SerLot.setText("");
//        Cantidad.setText("");
//        Cantidad.removeTextChangedListener(textWatcher)
//        Cantidad.clearFocus()
//        Cantidad.error = null
//    }
//    fun mostrarConfirmacionAsignarUbicacion(IdUser: String, IdUbi: String) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Confirmar ubicación: $IdUbi")
//        builder.setMessage("Se asignará la ubicación $IdUbi a los elementos contabilizados. ¿Desea continuar?")
//
//        builder.setPositiveButton("Confirmar") { _, _ ->
//            // Usuario hizo clic en "Confirmar", llama a la función asignarUbicacion
//            Validar.asignarUbicacion(IdUser, IdUbi) { success, message ->
//                if (success) {
//                    // La asignación fue exitosa, puedes realizar acciones adicionales si es necesario
//                    val toast = Toasty.success(requireContext(), message, Toasty.LENGTH_SHORT)
//
//                    val handler = Handler()
//                    handler.postDelayed({
//                        toast.cancel() // Cancela el toast después de medio segundo
//                    }, 400)
//
//                    toast.show()
//                    CleanForm()
//
//                    ScannInput.requestFocus()
//                } else {
//                    // Hubo un error en la asignación, maneja el error según sea necesario
//                    // Puedes mostrar un mensaje de error o realizar otras acciones
//                    Toasty.error(requireContext(), "Error: $message", Toasty.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        builder.setNegativeButton("Cancelar") { _, _ ->
//            // Usuario hizo clic en "Cancelar", no se hace nada
//        }
//        builder.show()
//    }
//
//    fun onCancelBtn(){
//        ScannInput.inputType = InputType.TYPE_NULL
//        ScannInput.requestFocus()
//        Cantidad.inputType = InputType.TYPE_NULL
//        Cantidad.isFocusable = false
//        Cantidad.isFocusableInTouchMode = false
//        Cantidad.setText("")
//        ButtonCancel.visibility = View.GONE
//        CleanForm()
//    }
//
//    fun mostrarConfirmacionReinicio(IdUser: String) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Reiniciar conteo")
//        builder.setMessage("Se reiniciará el conteo para la ubicación actual. ¿Desea continuar?")
//
//        builder.setPositiveButton("Confirmar") { _, _ ->
//            // Usuario hizo clic en "Confirmar", llama a la función asignarUbicacion
//            Validar.resetUbi(IdUser) { success, message ->
//                if (success) {
//                    // La asignación fue exitosa, puedes realizar acciones adicionales si es necesario
//                    val toast = Toasty.success(requireContext(), message, Toasty.LENGTH_SHORT)
//
//                    val handler = Handler()
//                    handler.postDelayed({
//                        toast.cancel() // Cancela el toast después de medio segundo
//                    }, 400)
//                    toast.show()
//                    CleanForm()
//                    ScannInput.requestFocus()
//                } else {
//                    // Hubo un error en la asignación, maneja el error según sea necesario
//                    // Puedes mostrar un mensaje de error o realizar otras acciones
//                    Toasty.error(requireContext(), "Error: $message", Toasty.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        builder.setNegativeButton("Cancelar") { _, _ ->
//            // Usuario hizo clic en "Cancelar", no se hace nada
//        }
//        builder.show()
//    }


}