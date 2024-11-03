package com.example.mydrawingapp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.google.firebase.firestore.FirebaseFirestore
//object UserSession {
//    var email: String = ""
//}
@Composable
fun SigninScreen(navController: NavController, viewModel: DrawingViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBEBEBE)) // Grayish background
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Start Drawing",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700), // Gold color for title text
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email Input Field
        TextField(
            value = email,
            onValueChange = { email = it },

            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E5E5), RoundedCornerShape(8.dp)) // Light gray for input fields
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Input Field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E5E5), RoundedCornerShape(8.dp)) // Light gray for input fields
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { signInUser(email, password, firebaseAuth, context, navController) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Login", color = Color(0xFFFFD700), fontSize = 18.sp) // Gold text
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Register Button
        OutlinedButton(
            onClick = { registerUser(email, password, firebaseAuth, context, navController) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black), // Black text for outlined button
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD700)) // Gold outline
        ) {
            Text(text = "Register", fontSize = 18.sp, color = Color.Black) // Black text
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
                // Ensure FirebaseAuth reflects the current user after login
                firebaseAuth.currentUser?.let { currentUser ->
                    val currentUserEmail = currentUser.email ?: ""

                    // Optionally log the current user's email to verify
                    println("Current logged-in user email: $currentUserEmail")

                    // Navigate to the main screen or drawing screen
                    navController.navigate("create_drawing") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } else {
                // Handle login failure
                Toast.makeText(
                    context,
                    "Authentication failed: ${task.exception?.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}

// Function to handle user registration
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
                val currentUser = firebaseAuth.currentUser
                currentUser?.let { user ->
                    val userId = user.uid
                    val currentUserEmail = user.email ?: "" // Update UserSession with current user

                    // Add user details to Firestore
                    val userMap = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .set(userMap)
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
                // Handle registration failure
                Toast.makeText(
                    context,
                    "Registration failed: ${task.exception?.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}
