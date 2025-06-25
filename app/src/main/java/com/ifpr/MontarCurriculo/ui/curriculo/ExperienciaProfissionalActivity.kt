package com.ifpr.MontarCurriculo.ui.curriculo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.MontarCurriculo.R
//import com.ifpr.MontarCurriculo.model.curriculo
//import com.ifpr.MontarCurriculo.model.ExperienciaProfissionalActivity
import com.ifpr.MontarCurriculo.ui.formacaoAcademica.FormacaoAcademicaActivity

@Suppress("DEPRECATION")
class ExperienciaProfissionalActivity : AppCompatActivity() {

    private lateinit var editTextEmpresa: EditText
    private lateinit var editTextCargo: EditText
    private lateinit var editTextPeriodoInicio: EditText // Adicionado
    private lateinit var editTextPeriodoFim: EditText   // Adicionado
    private lateinit var editTextDescricao: EditText    // Adicionado
    private lateinit var buttonProximoExperiencia: Button
    private lateinit var buttonAdicionarExperiencia: Button // Botão para adicionar mais experiências

   // private var currentCurriculo: curriculo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiencia) // Certifique-se que este é o layout correto

        // Receber o objeto Curriculo da Activity anterior
       // currentCurriculo = intent.getParcelableExtra("curriculoObject")

      //  if (currentCurriculo == null) {
            Toast.makeText(this, "Erro: Dados do currículo não encontrados.", Toast.LENGTH_LONG).show()
            finish() // Fecha a Activity se não houver dados essenciais
            return
      //  }

        // 1. Inicializar as views
        editTextEmpresa = findViewById(R.id.editTextEmpresa)
        editTextCargo = findViewById(R.id.editTextCargo)
        editTextPeriodoInicio = findViewById(R.id.editTextPeriodoInicio) // IDs dos novos campos
        editTextPeriodoFim = findViewById(R.id.editTextPeriodoFim)       // IDs dos novos campos
        editTextDescricao = findViewById(R.id.editTextDescricao)         // IDs dos novos campos
        buttonProximoExperiencia = findViewById(R.id.buttonProximoExperiencia)
        buttonAdicionarExperiencia = findViewById(R.id.buttonAdicionarExperiencia) // Novo botão

        // 2. Lógica para adicionar uma experiência e limpar os campos
        buttonAdicionarExperiencia.setOnClickListener {
            adicionarExperiencia()
        }

        // 3. Lógica para ir para a próxima tela
        buttonProximoExperiencia.setOnClickListener {
            // Adiciona a última experiência (se houver algo nos campos) antes de prosseguir
            adicionarExperiencia(true) // Passa true para forçar a adição e ir para a próxima tela

      //      if (currentCurriculo?.experiencias?.isEmpty() == true) {
                Toast.makeText(this, "Por favor, adicione pelo menos uma experiência profissional.", Toast.LENGTH_LONG).show()
         //   } else {
                Toast.makeText(this, "Experiências Profissionais Coletadas! Indo para a próxima tela.", Toast.LENGTH_SHORT).show()
                // Navegar para a próxima tela (FormacaoAcademicaActivity)
                val intent = Intent(this, FormacaoAcademicaActivity::class.java)
             //   intent.putExtra("curriculoObject", currentCurriculo) // Passa o objeto atualizado
                startActivity(intent)
            }
        }
  //  }

    private fun adicionarExperiencia(proximaTela: Boolean = false) {
        val empresa = editTextEmpresa.text.toString().trim()
        val cargo = editTextCargo.text.toString().trim()
        val periodoInicio = editTextPeriodoInicio.text.toString().trim()
        val periodoFim = editTextPeriodoFim.text.toString().trim()
        val descricao = editTextDescricao.text.toString().trim()

        if (empresa.isEmpty() || cargo.isEmpty() || periodoInicio.isEmpty() || periodoFim.isEmpty()) {
            Toast.makeText(this, "Empresa, Cargo e Período são campos obrigatórios.", Toast.LENGTH_LONG).show()
            return // Não adiciona a experiência se os campos obrigatórios não estiverem preenchidos
        }

       // val experiencia = ExperienciaProfissional(empresa, cargo, periodoInicio, periodoFim, descricao.ifEmpty { null })
      //  currentCurriculo?.experiencias?.add(experiencia)

        Toast.makeText(this, "Experiência adicionada: $empresa - $cargo", Toast.LENGTH_SHORT).show()

        // Limpar os campos para a próxima experiência
        editTextEmpresa.text.clear()
        editTextCargo.text.clear()
        editTextPeriodoInicio.text.clear()
        editTextPeriodoFim.text.clear()
        editTextDescricao.text.clear()

        // Opcional: mover o foco para o primeiro campo novamente
        editTextEmpresa.requestFocus()
    }
}