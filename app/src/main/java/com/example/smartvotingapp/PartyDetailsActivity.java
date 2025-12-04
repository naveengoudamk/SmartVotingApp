package com.example.smartvotingapp; // ✅ must match your manifest package name

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// ✅ Optional: add this import to ensure R resolves correctly
import com.example.smartvotingapp.R;

public class PartyDetailsActivity extends AppCompatActivity {

    private ImageView imgPartyLogo;
    private TextView tvPartyName, tvLeader, tvNominees, tvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);

        // Initialize Views
        imgPartyLogo = findViewById(R.id.imgPartyLogo);
        tvPartyName = findViewById(R.id.tvPartyName);
        tvLeader = findViewById(R.id.tvLeader);
        tvNominees = findViewById(R.id.tvNominees);
        tvHistory = findViewById(R.id.tvHistory);

        // Get selected party ID from intent
        String partyId = getIntent().getStringExtra("party_id");
        if (partyId != null) {
            loadPartyDetails(partyId);
        }
    }

    private void loadPartyDetails(String partyId) {
        switch (partyId) {

            case "BJP":
                imgPartyLogo.setImageResource(R.drawable.ic_bjp);
                tvPartyName.setText("Bharatiya Janata Party (BJP)");
                tvLeader.setText("Leader: Narendra Modi");
                tvNominees.setText("Key Nominees: Amit Shah, Rajnath Singh, Nirmala Sitharaman");
                tvHistory.setText("The Bharatiya Janata Party (BJP) was founded in 1980. It emerged from the Bharatiya Jana Sangh and has since become one of India's two major national parties. It advocates for nationalism and development-focused governance.");
                break;

            case "INC":
                imgPartyLogo.setImageResource(R.drawable.ic_inc);
                tvPartyName.setText("Indian National Congress (INC)");
                tvLeader.setText("Leader: Mallikarjun Kharge");
                tvNominees.setText("Key Nominees: Rahul Gandhi, Priyanka Gandhi Vadra, Sonia Gandhi");
                tvHistory.setText("The Indian National Congress (INC), founded in 1885, played a pivotal role in India's independence movement. It is one of the oldest political parties and advocates secularism, democracy, and social justice.");
                break;

            case "AAP":
                imgPartyLogo.setImageResource(R.drawable.ic_aap);
                tvPartyName.setText("Aam Aadmi Party (AAP)");
                tvLeader.setText("Leader: Arvind Kejriwal");
                tvNominees.setText("Key Nominees: Manish Sisodia, Atishi Marlena, Saurabh Bharadwaj");
                tvHistory.setText("The Aam Aadmi Party (AAP) was founded in 2012 by Arvind Kejriwal after the India Against Corruption movement. It promotes transparency, anti-corruption measures, and pro-people governance.");
                break;

            case "BSP":
                imgPartyLogo.setImageResource(R.drawable.ic_bsp);
                tvPartyName.setText("Bahujan Samaj Party (BSP)");
                tvLeader.setText("Leader: Mayawati");
                tvNominees.setText("Key Nominees: Satish Mishra, Danish Ali, Uma Shankar");
                tvHistory.setText("The Bahujan Samaj Party (BSP) was formed in 1984 by Kanshi Ram. It represents the Bahujan community including Scheduled Castes, Scheduled Tribes, and Other Backward Classes.");
                break;

            case "CPI":
                imgPartyLogo.setImageResource(R.drawable.ic_cpi);
                tvPartyName.setText("Communist Party of India (CPI)");
                tvLeader.setText("Leader: D. Raja");
                tvNominees.setText("Key Nominees: Binoy Viswam, Annie Raja, Kanhaiya Kumar");
                tvHistory.setText("The Communist Party of India (CPI) was founded in 1925. It advocates Marxist–Leninist ideology, working for labor rights, equality, and socialism.");
                break;

            case "DMK":
                imgPartyLogo.setImageResource(R.drawable.ic_dmk);
                tvPartyName.setText("Dravida Munnetra Kazhagam (DMK)");
                tvLeader.setText("Leader: M. K. Stalin");
                tvNominees.setText("Key Nominees: Kanimozhi Karunanidhi, T. R. Baalu, A. Raja");
                tvHistory.setText("The Dravida Munnetra Kazhagam (DMK) was founded in 1949 in Tamil Nadu. It promotes Dravidian ideals, social justice, and Tamil cultural pride.");
                break;

            case "TMC":
                imgPartyLogo.setImageResource(R.drawable.ic_tmc);
                tvPartyName.setText("All India Trinamool Congress (TMC)");
                tvLeader.setText("Leader: Mamata Banerjee");
                tvNominees.setText("Key Nominees: Abhishek Banerjee, Derek O’Brien, Sukhendu Sekhar Roy");
                tvHistory.setText("The All India Trinamool Congress (TMC) was formed in 1998 by Mamata Banerjee after splitting from the INC. It has a strong base in West Bengal and advocates secularism and grassroots democracy.");
                break;

            case "NCP":
                imgPartyLogo.setImageResource(R.drawable.ic_ncp);
                tvPartyName.setText("Nationalist Congress Party (NCP)");
                tvLeader.setText("Leader: Sharad Pawar");
                tvNominees.setText("Key Nominees: Ajit Pawar, Supriya Sule, Praful Patel");
                tvHistory.setText("The Nationalist Congress Party (NCP) was formed in 1999 by Sharad Pawar, P. A. Sangma, and Tariq Anwar. It promotes secularism, democracy, and federalism.");
                break;

            default:
                tvPartyName.setText("Unknown Party");
                tvLeader.setText("");
                tvNominees.setText("");
                tvHistory.setText("Party details not found.");
                break;
        }
    }
}
