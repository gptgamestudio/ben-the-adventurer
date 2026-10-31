package com.example.data.repository

import com.example.data.models.GameMateSync
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GameMateRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = firestore.collection("game_mates")

    fun getGameMateFlow(userId: String): Flow<GameMateSync?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = collection.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val mate = snapshot.toObject(GameMateSync::class.java)
                trySend(mate)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveGameMate(mate: GameMateSync) {
        if (mate.userId.isNotBlank()) {
            collection.document(mate.userId).set(mate).await()
        }
    }
}
