package com.tecsup.authfirebaseapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    //  Si el usuario no está logueado lo manda a Login
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    val db = FirebaseFirestore.getInstance()
    val cursosCollection = db.collection("cursos")

    // Estados
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cursoIdEditando by remember { mutableStateOf<String?>(null) }
    var cursos by remember { mutableStateOf<List<Curso>>(emptyList()) }

    //  Cargar cursos en tiempo real
    LaunchedEffect(user.uid) {
        cursosCollection.whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    cursos = snapshot.documents.map { doc ->
                        Curso(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                }
            }
    }

    //  limpiar formulario
    fun limpiarFormulario() {
        nombre = ""
        descripcion = ""
        cursoIdEditando = null
    }

    //  GUARDAR o EDITAR curso
    fun guardarCurso() {
        if (nombre.isBlank()) {
            Toast.makeText(context, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "userId" to user.uid
        )

        if (cursoIdEditando == null) {
            // Crear
            cursosCollection.add(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Curso agregado", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
        } else {
            // Actualizar
            cursosCollection.document(cursoIdEditando!!)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Curso actualizado", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
        }
    }

    //  ELIMINAR CURSO (ESTA ES LA FUNCIÓN QUE TE FALTABA)
    fun eliminarCurso(curso: Curso) {
        cursosCollection.document(curso.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Curso eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    //  UI
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // LOGO TECSUP AL CENTRO
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tecsup_logo),
                    contentDescription = "TECSUP",
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    "Programación Móvil – TECSUP",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TITULO + CERRAR SESION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mis Cursos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Cerrar sesión")
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text("Usuario: ${user.email}")

            Spacer(modifier = Modifier.height(16.dp))

            // FORMULARIO
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del curso") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { guardarCurso() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (cursoIdEditando == null) "Agregar" else "Guardar")
                }

                if (cursoIdEditando != null) {
                    OutlinedButton(
                        onClick = { limpiarFormulario() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(10.dp))

            Text(
                "Lista de cursos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cursos) { curso ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(curso.nombre, fontWeight = FontWeight.Bold)
                            if (curso.descripcion.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(curso.descripcion)
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                                OutlinedButton(onClick = {
                                    nombre = curso.nombre
                                    descripcion = curso.descripcion
                                    cursoIdEditando = curso.id
                                }) {
                                    Text("Editar")
                                }

                                OutlinedButton(onClick = {
                                    eliminarCurso(curso)   // ← YA NO MARCA ERROR
                                }) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
