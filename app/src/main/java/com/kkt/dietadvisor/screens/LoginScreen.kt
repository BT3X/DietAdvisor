package com.kkt.dietadvisor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@Composable
fun LoginScreen() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        // Define Screen Element Reference Variables
        val (
            txtWelcomeTo,
            txtDietAdvisor,
            infoCard,
            txtSeparator,
            btnSignInWithGoogle,
            btnSignInWithLine,
            txtNotAMember,
            txtSignUpNow,
        ) = createRefs()

        // Define Layout & Link Components
        Text(
            text = "Welcome to",
            fontSize = 24.sp,
            modifier = Modifier.constrainAs(txtWelcomeTo) {
                top.linkTo(parent.top, margin = 100.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })
        Text(
            text = "Diet Advisor",
            fontSize = 35.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.constrainAs(txtDietAdvisor) {
                top.linkTo(txtWelcomeTo.bottom, margin = 10.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })
        Surface(
            modifier = Modifier.constrainAs(infoCard) {
                top.linkTo(txtDietAdvisor.bottom, margin = 40.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 12.dp
                    )
                ) {
                    Text(text = "Username", modifier = Modifier.padding(end = 20.dp))
                    TextField(value = "Username", onValueChange = { /* TODO: */ })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 18.dp
                    )
                ) {
                    Text(text = "Password", modifier = Modifier.padding(end = 20.dp))
                    TextField(value = "Password", onValueChange = { /* TODO: */ })
                }

                Button(
                    onClick = { /*TODO*/ },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }
        }

        // Separator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .constrainAs(txtSeparator) {
                    top.linkTo(infoCard.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
        {
            Box(modifier = Modifier.weight(1f)) {
                HorizontalDivider(color = Color.Gray)
            }
            Text(
                text = "or",
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 18.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                HorizontalDivider(color = Color.Gray)
            }
        }

        // Alternative Sign-In Methods
        Button(
            modifier = Modifier.constrainAs(btnSignInWithGoogle) {
                top.linkTo(txtSeparator.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            shape = RoundedCornerShape(8.dp),
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "Sign In with Google",
                fontSize = 18.sp
            )
        }

        Button(
            modifier = Modifier.constrainAs(btnSignInWithLine) {
                top.linkTo(btnSignInWithGoogle.bottom)
                start.linkTo(btnSignInWithGoogle.start)
                end.linkTo(btnSignInWithGoogle.end)
                width = Dimension.fillToConstraints
            },
            shape = RoundedCornerShape(8.dp),
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "Sign In with Line",
                fontSize = 18.sp
            )
        }

        val annotatedString = buildAnnotatedString {
            append("Not a member? ")

            // Push a string annotation that contains clickable text
            pushStringAnnotation(
                tag = "SIGN_UP",
                annotation = "signUp"
            )

            // Apply a span style to achieve the underline
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Sign up now")
            }

            pop() // Finalize the annotated string by popping it
        }

        // Make the text clickable (only the underlined part)
//        Text(
//            text = annotatedString,
//            fontSize = 16.sp,
//            modifier = Modifier.constrainAs(txtNotAMember) {
//                top.linkTo(btnSignInWithLine.bottom)
//            },
//            onClick = { offset ->
//                // Perform an action based on the click location
//                annotatedString.getStringAnnotations(tag = "signup", start = offset, end = offset)
//                    .firstOrNull()?.let {
//                        // Handle the "Sign up now" click here
//                        println("Clicked 'Sign up now'")
//                    }
//            }
//        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DietAdvisorTheme {
        LoginScreen()
    }
}
