package com.ifpr.MontarCurriculo.ui.curriculo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.MontarCurriculo.R
import com.ifpr.MontarCurriculo.baseclasses.CurriculoModel
import com.ifpr.MontarCurriculo.baseclasses.Experiencia
import com.ifpr.MontarCurriculo.ui.formacaoAcademica.FormacaoAcademicaActivity

class ExperienciaProfissionalActivity : AppCompatActivity() {

    private lateinit var editTextEmpresa: EditText
    private lateinit var editTextCargo: EditText
    private lateinit var editTextPeriodoInicio: EditText
    private lateinit var editTextPeriodoFim: EditText
    private lateinit var editTextDescricao: EditText
    private lateinit var buttonProximoExperiencia: Button
    private lateinit var buttonAdicionarExperiencia: Button
    private lateinit var buttonPularExperiencia: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiencia)

        // Inicializar views
        editTextEmpresa = findViewById(R.id.editTextEmpresa)
        editTextCargo = findViewById(R.id.editTextCargo)
        editTextPeriodoInicio = findViewById(R.id.editTextPeriodoInicio)
        editTextPeriodoFim = findViewById(R.id.editTextPeriodoFim)
        editTextDescricao = findViewById(R.id.editTextDescricao)
        buttonProximoExperiencia = findViewById(R.id.buttonProximoExperiencia)
        buttonAdicionarExperiencia = findViewById(R.id.buttonAdicionarExperiencia)
        buttonPularExperiencia = findViewById(R.id.buttonPularExperiencia)

        // Configurar Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("curriculos")

        // Receber ID do currículo
        currentCurriculoId = intent.getStringExtra("curriculoId")
        if (currentCurriculoId == null) {
            Toast.makeText(this, "Erro: ID do currículo não fornecido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Carregar dados do currículo
        loadCurriculoData(currentCurriculoId!!)

        // Listeners dos botões
        buttonAdicionarExperiencia.setOnClickListener {
            adicionarExperiencia()
        }

        buttonProximoExperiencia.setOnClickListener {
            // Tenta adicionar experiência atual antes de prosseguir
            if (camposPreenchidos()) {
                adicionarExperiencia()
            }
            navegarParaFormacaoAcademica()
        }

        // Botão para pular experiências
        buttonPularExperiencia.setOnClickListener {
            Toast.makeText(this, "Experiências profissionais puladas.", Toast.LENGTH_SHORT).show()
            navegarParaFormacaoAcademica()
        }
    }

    private fun loadCurriculoData(curriculoId: String) {
        databaseReference.child(curriculoId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
            } else {
                Toast.makeText(this, "Currículo não encontrado.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar currículo.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun camposPreenchidos(): Boolean {
        val empresa = editTextEmpresa.text.toString().trim()
        val cargo = editTextCargo.text.toString().trim()
        val periodoInicio = editTextPeriodoInicio.text.toString().trim()
        val periodoFim = editTextPeriodoFim.text.toString().trim()
        val descricao = editTextDescricao.text.toString().trim()

        return empresa.isNotEmpty() || cargo.isNotEmpty() ||
                periodoInicio.isNotEmpty() || periodoFim.isNotEmpty() ||
                descricao.isNotEmpty()
    }

    private fun adicionarExperiencia() {
        val empresa = editTextEmpresa.text.toString().trim()
        val cargo = editTextCargo.text.toString().trim()
        val periodoInicio = editTextPeriodoInicio.text.toString().trim()
        val periodoFim = editTextPeriodoFim.text.toString().trim()
        val descricao = editTextDescricao.text.toString().trim()

        // Verifica se pelo menos um campo foi preenchido
        if (empresa.isEmpty() && cargo.isEmpty() && periodoInicio.isEmpty() &&
            periodoFim.isEmpty() && descricao.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos um campo.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria a experiência
        val experiencia = Experiencia(
            empresa = if (empresa.isNotEmpty()) empresa else null,
            cargo = if (cargo.isNotEmpty()) cargo else null,
            periodo = if (periodoInicio.isNotEmpty() || periodoFim.isNotEmpty())
                "$periodoInicio - $periodoFim" else null,
            descricao = if (descricao.isNotEmpty()) descricao else null
        )

        currentCurriculo?.let { curriculo ->
            val experienciasMutavel = curriculo.experiencias?.toMutableList() ?: mutableListOf()
            experienciasMutavel.add(experiencia)
            curriculo.experiencias = experienciasMutavel

            // Salvar no Firebase
            databaseReference.child(currentCurriculoId!!).setValue(curriculo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Experiência adicionada!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } ?: run {
            Toast.makeText(this, "Erro: Currículo não disponível.", Toast.LENGTH_LONG).show()
        }
    }

    private fun limparCampos() {
        editTextEmpresa.text.clear()
        editTextCargo.text.clear()
        editTextPeriodoInicio.text.clear()
        editTextPeriodoFim.text.clear()
        editTextDescricao.text.clear()
    }

    private fun navegarParaFormacaoAcademica() {
        val intent = Intent(this, FormacaoAcademicaActivity::class.java)
        intent.putExtra("curriculoId", currentCurriculoId)
        startActivity(intent)
        finish()
    }
}