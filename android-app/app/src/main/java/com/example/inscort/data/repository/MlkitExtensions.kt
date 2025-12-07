package com.example.inscort.data.repository


import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase / ML Kit Task 를 suspend 함수처럼 await 하기 위한 확장 함수
 */
suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            cont.resume(result)
        }.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }

        /*
        cont.invokeOnCancellation {
            this.cancel()
        }
         */
    }