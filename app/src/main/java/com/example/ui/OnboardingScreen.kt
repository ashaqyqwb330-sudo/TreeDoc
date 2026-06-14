package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: String
)

@Composable
fun OnboardingScreen(
    onDismiss: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    
    val pages = listOf(
        OnboardingPageData(
            title = "أهلاً بك في المعمار الشجري 🌳",
            description = "أداة متطورة تمكنك من مسح وفحص المجلدات على جهازك وتوليد تقارير شجرية منسقة بصيغ متعددة مثل JSON, HTML, PDF, Markdown, TXT.",
            icon = "📊"
        ),
        OnboardingPageData(
            title = "دعم جميع مصادر التخزين 💾",
            description = "سواء كانت الملفات على ذاكرتك الداخلية، بطاقة SD خارجية، فلاشة USB (OTG)، أو مسار مخصص تقوم بإدخاله يدوياً، يمكن للتطبيق تصفحها بالكامل.",
            icon = "🔌"
        ),
        OnboardingPageData(
            title = "حل الوصول للمجلدات المحمية 🔑",
            description = "تجاوز قيود أندرويد لتصفح مجلدات النظام الحساسة مثل Android/data و Android/obb عبر المصفح المخصص المدمج، واختصارات المسارات المباشرة مع الحافظة.",
            icon = "🔓"
        ),
        OnboardingPageData(
            title = "تقارير JSON و تفاصيل متقدمة ⚙️",
            description = "قم بتخصيص تقرير JSON بالكامل؛ فعّل الخيارات الإضافية مثل امتداد الملف، تاريخ التعديل بصيغة ISO، الصلاحيات rwx، نوع MIME، وعمق الفروع في المجلدات وحساب MD5.",
            icon = "🧠"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1C1B1F),
                        Color(0xFF2B2930)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated slide content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "slide_onboarding"
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = page.icon,
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFCAC4D0),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dots Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentPage == index) Color(0xFFD0BCFF)
                                else Color(0xFF49454F)
                            )
                    )
                }
            }

            // Bottom control actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCAC4D0))
                ) {
                    Text("تخطي השرح ⏭️", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    )
                ) {
                    Text(
                        text = if (currentPage == pages.lastIndex) "ابدأ الآن 🚀" else "التالي ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
