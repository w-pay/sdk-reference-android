package au.com.woolworths.village.sdk.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import au.com.woolworths.village.sdk.app.databinding.PaymentReceiptBinding
import au.com.woolworths.village.sdk.model.*
import kotlinx.android.synthetic.main.receipt_row.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.text.NumberFormat

const val PAYMENT = "au.com.woolworths.village.app.Payment"
const val INSTRUMENT = "au.com.woolworths.village.app.Instrument"

class PaymentReceipt : AppCompatActivity() {
    private lateinit var bindings: PaymentReceiptBinding
    private lateinit var basketItemsAdapter: RecyclerView.Adapter<*>
    private lateinit var basketItemsManager: RecyclerView.LayoutManager

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings = PaymentReceiptBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindPaymentToReceipt()
        bindInstrumentToReceipt()
    }

    private fun bindPaymentToReceipt() {
        val payment: CustomerPaymentRequest = intent.getSerializableExtra(PAYMENT) as CustomerPaymentRequest
        basketItemsAdapter = payment.basket?.items?.let { BasketItemsAdapter(it) }!!
        basketItemsManager = LinearLayoutManager(this)

        val amount = currencyFormat.format(payment.grossAmount)
        bindings.amountPaid.text = amount
        bindings.basketItems.apply {
            layoutManager = basketItemsManager
            adapter = basketItemsAdapter
        }

        bindings.totalRow.item.text = getString(R.string.items_count).format(payment.basket?.items?.size)
        bindings.totalRow.amount.text = amount

        bindings.taxRow.item.text = getString(R.string.gst_heading)
        bindings.taxRow.amount.text = currencyFormat.format(calculateGST(payment.grossAmount))
    }

    private fun bindInstrumentToReceipt() {
        val paymentInstrument: String = intent.getStringExtra(INSTRUMENT)

        bindings.paymentInstrument.text =
            toUtf8(getString(R.string.instrument_details).format(paymentInstrument))
    }

    private fun calculateGST(amount: BigDecimal): BigDecimal {
        return amount.divide(BigDecimal(11), 2, RoundingMode.HALF_UP)
    }

    private fun toUtf8(str: String) =
        String(str.toByteArray(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"))

    class BasketItemsAdapter(private val items: List<Basket.Items>) : RecyclerView.Adapter<BasketItemsAdapter.ItemViewHolder>() {
        class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.receipt_row, parent, false)

            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]

            holder.itemView.item.text = item.label
            holder.itemView.amount.text = currencyFormat.format(item.totalPrice)
        }

        override fun getItemCount(): Int = items.size
    }
}