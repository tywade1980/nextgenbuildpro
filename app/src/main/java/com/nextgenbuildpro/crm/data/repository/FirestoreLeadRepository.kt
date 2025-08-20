package com.nextgenbuildpro.crm.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nextgenbuildpro.crm.data.model.Lead
import kotlinx.coroutines.tasks.await

class FirestoreLeadRepository {
    private val db = FirebaseFirestore.getInstance()
    private val leadsCollection = db.collection("leads")
    
    suspend fun getLeads(): List<Lead> {
        return leadsCollection.get().await().toObjects(Lead::class.java)
    }
    
    suspend fun saveLead(lead: Lead) {
        leadsCollection.document(lead.id).set(lead).await()
    }
    
    // other operations
}