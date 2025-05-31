package com.ifpr.MontarCurriculo.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ifpr.MontarCurriculo.databinding.FragmentHomeBinding
import android.util.Base64
import android.widget.* // Importa Button e outras classes de widget
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.MontarCurriculo.R
import com.ifpr.MontarCurriculo.baseclasses.Item
import android.content.Intent // Importar Intent para iniciar a próxima Activity
import com.ifpr.MontarCurriculo.ui.curriculo.DadosPessoaisActivity // Substitua por sua Activity real

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false) // Infla o binding
        val root: View = binding.root // Pega a view raiz do binding

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java) // Exemplo, pode ser diferente no seu projeto

        val containerLayout = root.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItensMarketplace(containerLayout)

        val switch = root.findViewById<SwitchCompat>(R.id.darkModeSwitch)
        habilitaDarkMode(switch)

        // Encontrar o novo botão pelo ID e configurar o OnClickListener
        val buttonCriarNovoCurriculo: Button = root.findViewById(R.id.buttonCriarNovoCurriculo)
        buttonCriarNovoCurriculo.setOnClickListener {
            // Ação para o botão: Iniciar a Activity de criação de currículo
            val intent = Intent(context, DadosPessoaisActivity::class.java) // Substitua 'DadosPessoaisActivity' pela Activity onde o usuário começa a criar o currículo.
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun carregarItensMarketplace(container: LinearLayout) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java) ?: continue

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)

                        enderecoView.text = "Endereço: ${item.endereco ?: "Não informado"}"

                        if (!item.imageUrl.isNullOrEmpty()) {
                            Glide.with(container.context).load(item.imageUrl).into(imageView)
                        } else if (!item.base64Image.isNullOrEmpty()) {
                            try {
                                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imageView.setImageBitmap(bitmap)
                            } catch (_: Exception) {}
                        }

                        container.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(container.context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun habilitaDarkMode(switch: SwitchCompat){

        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Estado salvo
        val darkMode = prefs.getBoolean("dark_mode", false)
        switch.isChecked = darkMode
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Listener de mudança
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}