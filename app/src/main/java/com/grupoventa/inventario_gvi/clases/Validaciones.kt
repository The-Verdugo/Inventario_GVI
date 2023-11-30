package com.grupoventa.inventario_gvi.clases

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grupoventa.inventario_gvi.data.models.ItemSAP
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Validaciones {
    // Construir la ruta del nodo en Firebase
    private val nodePath = "/Inventarios/${obtenerFechaActual()}/inventario_inicial"

    // Obtener la referencia al nodo en Firebase y aplicar la consulta
    private val database = FirebaseDatabase.getInstance()
    val reference = database.getReference(nodePath)

    fun obtenerFechaActual(): String {
        return SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
    }

    fun ValidarExistenciaLot(ItemCode: String, Lote: String, callback: (Boolean, ItemSAP?) -> Unit) {
        // Escuchador para obtener datos de la consulta
        reference.orderByChild("ItemCode").equalTo(ItemCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verificar si se encontraron resultados
                    if (dataSnapshot.exists()) {
                        // Iterar sobre los hijos para buscar la coincidencia exacta de DistNumber (Lote)
                        for (childSnapshot in dataSnapshot.children) {
                            val objDto = childSnapshot.getValue(ItemSAP::class.java)

                            // Verificar la coincidencia del campo DistNumber (Lote)
                            if (objDto?.DistNumber == Lote) {
                                callback(true, objDto)
                                return // Terminar la función ya que encontramos una coincidencia
                            }
                        }

                        // No se encontró coincidencia para el campo DistNumber (Lote)
                        callback(false, null)
                    } else {
                        // El Item no existe en Firebase
                        callback(false, null)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores
                    callback(false, null)
                }
            })
    }

    fun ValidarExistenciaSKU(SKU_BAR: String, callback: (Boolean, ItemSAP?) -> Unit) {
        // Primera consulta por el campo CodeBars
        reference.orderByChild("CodeBars").equalTo(SKU_BAR)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verificar si se encontraron resultados
                    if (dataSnapshot.exists()) {
                        // Iterar sobre los hijos para buscar la coincidencia exacta de CodeBars
                        for (childSnapshot in dataSnapshot.children) {
                            val objDto = childSnapshot.getValue(ItemSAP::class.java)
                            callback(true, objDto)
                            return // Terminar la función ya que encontramos una coincidencia
                        }
                    }

                    // Si no se encontró coincidencia en CodeBars, realizar la segunda consulta por el campo ItemCode
                    reference.orderByChild("ItemCode").equalTo(SKU_BAR)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // Verificar si se encontraron resultados
                                if (dataSnapshot.exists()) {
                                    // Iterar sobre los hijos para buscar la coincidencia exacta de ItemCode
                                    for (childSnapshot in dataSnapshot.children) {
                                        val objDto = childSnapshot.getValue(ItemSAP::class.java)
                                        callback(true, objDto)
                                        return // Terminar la función ya que encontramos una coincidencia
                                    }
                                }

                                // No se encontró coincidencia ni en CodeBars ni en ItemCode
                                callback(false, null)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Manejar errores
                                callback(false, null)
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores
                    callback(false, null)
                }
            })
    }


    fun ValidarExistenciaIM(Serie: String, callback: (Boolean, ItemSAP?) -> Unit) {
        // Escuchador para obtener datos de la consulta
        reference.orderByChild("DistNumber").equalTo(Serie)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Verificar si se encontraron resultados
                if (dataSnapshot.exists()) {
                    // La serie existe en Firebase
                    val firstChild = dataSnapshot.children.first()
                    // Obtener el objeto ItemSAP a partir del primer hijo
                    val objDto = firstChild.getValue(ItemSAP::class.java)
                    callback(true,objDto)
                } else {
                    // La serie no existe en Firebase
                    callback(false,null)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores
                callback(false, null)
            }
        })
    }

    fun registrarConteoSerie(
        cantidad: Int,
        data: ItemSAP,
        idUser: String,
        onComplete: (exitoso: Boolean, mensaje: String) -> Unit
    ) {
        // Obtener la fecha actual con el formato dd_MM_yyyy
        val fechaActual = obtenerFechaActual()

        val pathConteo ="Inventarios/$fechaActual/conteo"
        val reference = database.getReference(pathConteo)
        // Obtener el valor actual de num_conteo
        reference.child("num_conteo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numConteoActual = snapshot.getValue(Int::class.java) ?: 1

                // Verificar si ya existe un registro con el mismo DistNumber dentro del conteo actual
                reference.child(numConteoActual.toString())
                    .orderByChild("DistNumber")
                    .equalTo(data.DistNumber)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(innerSnapshot: DataSnapshot) {
                            if (innerSnapshot.exists()) {
                                // Ya existe un registro con el mismo DistNumber
                                onComplete(
                                    false,
                                    "Error: La serie ya se encuentra registrada."
                                )
                            } else {
                                // Crear un objeto para almacenar los datos del conteo
                                val conteoData = hashMapOf(
                                    "DistNumber" to data.DistNumber,
                                    "FirmName" to data.FirmName,
                                    "ItmsGrpNam" to data.ItmsGrpNam,
                                    "U_CATEGORIA" to data.U_CATEGORIA,
                                    "Ubicacion" to "", // Reemplazar por el campo correcto
                                    "WhsCode" to data.WhsCode,
                                    "cantidad" to cantidad,
                                    "idUser" to idUser,
                                    "itemCode" to data.ItemCode,
                                    "itemName" to data.itemName
                                )

                                // Registrar el conteo en la base de datos
                                reference.child(numConteoActual.toString()).push()
                                    .setValue(conteoData)
                                    .addOnSuccessListener {
                                        // Operación exitosa
                                        onComplete(
                                            true,
                                            "Item registrado exitosamente"
                                        )
                                    }
                                    .addOnFailureListener {
                                        // Error al registrar
                                        onComplete(
                                            false,
                                            "Error al registrar el conteo: ${it.message}"
                                        )
                                    }
                            }
                        }

                        override fun onCancelled(innerError: DatabaseError) {
                            // Manejar errores
                            onComplete(
                                false,
                                "Error al verificar la existencia del registro: ${innerError.message}"
                            )
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
                onComplete(
                    false,
                    "Error al obtener el valor de num_conteo: ${error.message}"
                )
            }
        })
    }

    fun registrarConteoSKU(
        cantidad: Int,
        data: ItemSAP,
        idUser: String,
        onComplete: (exitoso: Boolean, mensaje: String) -> Unit
    ) {
        // Obtener la fecha actual con el formato dd_MM_yyyy
        val fechaActual = obtenerFechaActual()

        val pathConteo ="Inventarios/$fechaActual/conteo"
        val reference = database.getReference(pathConteo)
        // Obtener el valor actual de num_conteo
        reference.child("num_conteo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numConteoActual = snapshot.getValue(Int::class.java) ?: 1

                // Crear un objeto para almacenar los datos del conteo
                val conteoData = hashMapOf(
                    "DistNumber" to data.DistNumber,
                    "FirmName" to data.FirmName,
                    "ItmsGrpNam" to data.ItmsGrpNam,
                    "U_CATEGORIA" to data.U_CATEGORIA,
                    "Ubicacion" to "", // Reemplazar por el campo correcto
                    "WhsCode" to data.WhsCode,
                    "cantidad" to cantidad,
                    "idUser" to idUser,
                    "itemCode" to data.ItemCode,
                    "itemName" to data.itemName
                )

                // Registrar el conteo en la base de datos
                reference.child(numConteoActual.toString()).push()
                    .setValue(conteoData)
                    .addOnSuccessListener {
                        // Operación exitosa
                        onComplete(
                            true,
                            "Item registrado exitosamente"
                        )
                    }
                    .addOnFailureListener {
                        // Error al registrar
                        onComplete(
                            false,
                            "Error al registrar el conteo: ${it.message}"
                        )
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
                onComplete(
                    false,
                    "Error al obtener el valor de num_conteo: ${error.message}"
                )
            }
        })
    }

    fun resetUbi(IdUser: String, onComplete: (success: Boolean, message: String) -> Unit) {
        // Obtener la fecha actual con el formato dd_MM_yyyy
        val fechaActual = obtenerFechaActual()

        // Crear la referencia a la ubicación específica
        val conteoRef: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("Inventarios/$fechaActual/conteo")

        conteoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verificar si el snapshot tiene datos
                if (snapshot.exists()) {
                    // Obtener el valor actual de num_conteo
                    val numConteoActual =
                        snapshot.child("num_conteo").getValue(Int::class.java) ?: 1

                    // Acceder al nodo correspondiente a num_conteo
                    val conteoActual = snapshot.child(numConteoActual.toString())

                    for (conteoSnapshot in conteoActual.children) {
                        // Obtener el valor de Ubicación
                        val ubicacion =
                            conteoSnapshot.child("Ubicacion").getValue(String::class.java)

                        // Verificar si el IdUser coincide y la Ubicación está vacía
                        if (ubicacion == "" && conteoSnapshot.child("idUser")
                                .getValue(String::class.java) == IdUser
                        ) {
                            // Eliminar el nodo
                            conteoSnapshot.ref.removeValue()
                        }
                    }
                    onComplete(true, "Ubicación reestablecida correctamente")
                } else {
                    onComplete(false, "No hay datos en la ubicación especificada")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Manejar errores al obtener datos
                onComplete(false, "Error al obtener datos: ${error.message}")
            }
        })
    }




    fun asignarUbicacion(IdUser: String, IdUbi: String, onComplete: (success: Boolean, message: String) -> Unit) {
        // Obtener la fecha actual con el formato dd_MM_yyyy
        val fechaActual = obtenerFechaActual()

        val conteoRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inventarios/$fechaActual/conteo")

        // Obtener el valor actual de num_conteo
        conteoRef.child("num_conteo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numConteoActual = snapshot.getValue(Int::class.java) ?: 1
                var registrosEditados:Int = 0
                // Buscar nodos sin valor en el campo "Ubicación" dentro del conteo actual
                conteoRef.child(numConteoActual.toString())
                    .orderByChild("Ubicacion")
                    .equalTo("")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(innerSnapshot: DataSnapshot) {
                            // Verificar si existen nodos sin valor en el campo "Ubicación"
                            if (innerSnapshot.exists()) {
                                // Procesar los nodos encontrados
                                for (conteoSnapshot in innerSnapshot.children) {
                                    // Validar que el IdUser coincida antes de asignar la ubicación
                                    val userIdFromSnapshot = conteoSnapshot.child("idUser").getValue(String::class.java)
                                    if (userIdFromSnapshot == IdUser) {
                                        // Asignar el valor de IdUbi al campo "Ubicación"
                                        conteoSnapshot.ref.child("Ubicacion").setValue(IdUbi)
                                        registrosEditados++
                                    }
                                }
                                if (registrosEditados > 0){
                                onComplete(true, "Ubicación asignada correctamente")}
                                else{
                                    onComplete(false, "No se encontraron registros, valida que existan items contabilizados")
                                }
                            } else {
                                onComplete(false, "No se encontraron registros, valida que existan items contabilizados")
                            }
                        }

                        override fun onCancelled(innerError: DatabaseError) {
                            // Manejar errores al buscar nodos
                            onComplete(false, "Error al buscar nodos: ${innerError.message}")
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores al obtener el valor de num_conteo
                onComplete(false, "Error al obtener el valor de num_conteo: ${error.message}")
            }
        })
    }
}