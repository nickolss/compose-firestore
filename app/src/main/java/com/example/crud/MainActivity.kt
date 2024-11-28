package com.example.crud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.crud.ui.theme.CrudTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FirebaseFirestore
import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.firestore



class MainActivity : ComponentActivity() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrudTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black  // Fundo preto para a tela
                ) {
                    App(db)
                }

            }
        }
    }
}

@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var nomeError by remember { mutableStateOf(false) }
    var telefoneError by remember { mutableStateOf(false) }

    // Função de validação
    fun validateForm(): Boolean {
        var isValid = true

        if (nome.isBlank()) {
            nomeError = true
            isValid = false
        } else {
            nomeError = false
        }

        if (telefone.isBlank()) {
            telefoneError = true
            isValid = false
        } else {
            telefoneError = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Black) // Fundo da coluna principal preto
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
        // Título
        Text(
            text = "Cadastro de Cliente",
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Campo Nome
        CustomInputField(
            label = "Nome",
            value = nome,
            onValueChange = { nome = it },
            isError = nomeError,
            errorMessage = if (nomeError) "Nome não pode ser vazio" else ""
        )

        // Campo Telefone
        CustomInputField(
            label = "Telefone",
            value = telefone,
            onValueChange = { telefone = it },
            isError = telefoneError,
            errorMessage = if (telefoneError) "Telefone não pode ser vazio" else ""
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Cadastro
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (validateForm()) {
                        // Se o formulário for válido, envia os dados
                        val pessoa = hashMapOf(
                            "nome" to nome,
                            "telefone" to telefone
                        )

                        db.collection("Clientes").add(pessoa)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "Inserção realizada com sucesso: ${documentReference.id}")
                            }.addOnFailureListener { e ->
                                Log.w(TAG, "Inserção com o seguinte erro:", e)
                            }

                        // Limpa os campos após o envio
                        nome = ""
                        telefone = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))  // Cor roxa
            ) {
                Text(
                    text = "Cadastrar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Clientes
        val clientes = remember { mutableStateListOf<HashMap<String, String>>() }

        db.collection("Clientes").addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.w(TAG, "Error getting documents: ", exception)
                return@addSnapshotListener
            }

            clientes.clear()
            documents?.forEach { document ->
                val lista = hashMapOf(
                    "nome" to "${document.data["nome"]}",
                    "telefone" to "${document.data["telefone"]}"
                )
                clientes.add(lista)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(clientes) { cliente ->
                ClientCard(cliente)
            }
        }
    }
}

@Composable
fun CustomInputField(label: String, value: String, onValueChange: (String) -> Unit, isError: Boolean = false, errorMessage: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    Color(0xFF2C2C2C), RoundedCornerShape(8.dp)
                ), // Cor escura para os campos
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            singleLine = true,
            isError = isError
        )

        if (isError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ClientCard(cliente: HashMap<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)) // Cor escura para o cartão
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nome: ${cliente["nome"] ?: "--"}",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
            Text(
                text = "Telefone: ${cliente["telefone"] ?: "--"}",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }
    }
}
