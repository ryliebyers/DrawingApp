package com.example.mydrawingapp


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SigninScreen(navController: NavController, viewModel: DrawingViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(onClick = { signInUser(email, password, firebaseAuth, context, navController) }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Register Button
        Button(onClick = { registerUser(email, password, firebaseAuth, context, navController) }) {
            Text("Register")
        }
    }
}

// Function to handle user login
private fun signInUser(
    email: String,
    password: String,
    firebaseAuth: FirebaseAuth,
    context: android.content.Context,
    navController: NavController
) {
    firebaseAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("create_drawing") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Toast.makeText(
                    context,
                    "Authentication failed: ${task.exception?.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}

private fun registerUser(
    email: String,
    password: String,
    firebaseAuth: FirebaseAuth,
    context: android.content.Context,
    navController: NavController
) {
    firebaseAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = firebaseAuth.currentUser?.uid

                if (userId != null) {
                    // Add user details to Firestore
                    val user = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            navController.navigate("create_drawing") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Firestore Error: ${e.localizedMessage ?: "Unknown error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(
                    context,
                    "Registration failed: ${task.exception?.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}