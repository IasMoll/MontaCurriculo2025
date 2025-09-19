package com.ifpr.MontarCurriculo.ui.curriculo // <- Garanta que este package está EXATAMENTE assim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.MontarCurriculo.R
import com.ifpr.MontarCurriculo.baseclasses.CurriculoModel // <- Import correto para CurriculoModel
import com.ifpr.MontarCurriculo.ui.curriculo.ExperienciaProfissionalActivity // <- Import correto para a próxima tela

class ContatoActivity : AppCompatActivity() {

    private lateinit var editTextTelefone: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextLinkedIn: EditText
    private lateinit var buttonProximoContato: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null // <- Agora está usando CurriculoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contato) // Garanta que o seu arquivo de layout é activity_contato.xml

        editTextTelefone = findViewById(R.id.editTextTelefone) // <- Confirme que este ID está no activity_contato.xml
        editTextEmail = findViewById(R.id.editTextEmail)       // <- Confirme que este ID está no activity_contato.xml
        editTextLinkedIn = findViewById(R.id.editTextLinkedIn) // <- Confirme que este ID está no activity_contato.xml
        buttonProximoContato = findViewById(R.id.buttonProximoContato) // <- Confirme que este ID está no activity_contato.xml

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("curriculos")

        currentCurriculoId = intent.getStringExtra("curriculoId")
        if (currentCurriculoId == null) {
            Toast.makeText(this, "Erro: ID do currículo não fornecido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadCurriculoData(userId, currentCurriculoId!!)

        buttonProximoContato.setOnClickListener {
            saveContatoAndNavigate()
        }
    }

    private fun loadCurriculoData(userId: String, curriculoId: String) {
        databaseReference.child(curriculoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
                    currentCurriculo?.let {
                        editTextTelefone.setText(it.telefone)
                        editTextEmail.setText(it.email)
                        editTextLinkedIn.setText(it.linkedIn)
                    }
                } else {
                    Log.w("ContatoActivity", "Currículo não encontrado para edição. Criando um novo CurriculoModel.")
                    Toast.makeText(this@ContatoActivity, "Currículo não encontrado. Iniciando novo.", Toast.LENGTH_SHORT).show()
                    currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContatoActivity", "Erro ao carregar currículo: ${error.message}")
                Toast.makeText(this@ContatoActivity, "Erro ao carregar dados do currículo.", Toast.LENGTH_SHORT).show()
                currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
            }
        })
    }

    private fun saveContatoAndNavigate() {
        val telefone = editTextTelefone.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val linkedIn = editTextLinkedIn.text.toString().trim()

        if (telefone.isEmpty() || email.isEmpty() || linkedIn.isEmpty()) {
            Toast.makeText(this, "Todos os campos de contato são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        currentCurriculo?.apply {
            this.telefone = telefone
            this.email = email
            this.linkedIn = linkedIn
        }

        currentCurriculo?.let { curriculo ->
            currentCurriculoId?.let { id ->
                databaseReference.child(id).setValue(curriculo)
                    .addOnSuccessListener {
                        Log.d("ContatoActivity", "Dados de contato salvos/atualizados com sucesso.")
                        val intent = Intent(this, ExperienciaProfissionalActivity::class.java)
                        intent.putExtra("curriculoId", id)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar dados de contato: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("ContatoActivity", "Erro ao salvar dados de contato: ${e.message}")
                    }
            }
        } ?: run {
            Toast.makeText(this, "Erro: Currículo não disponível para salvar contato.", Toast.LENGTH_LONG).show()
        }
    }
}