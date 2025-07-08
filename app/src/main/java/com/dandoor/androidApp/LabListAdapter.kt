package com.dandoor.androidApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dandoor.ddlib.data.entity.Lab
import java.text.SimpleDateFormat
import java.util.*

/**
 * 실험(Lab) 데이터 리스트를 표시하는 RecyclerView 어댑터
 * Library 모듈의 Lab 엔티티를 사용하여 사이드바에 실험 목록을 표시
 */
class LabListAdapter(
    private val labList: List<Lab>,
    private val onItemClick: (Lab) -> Unit
) : RecyclerView.Adapter<LabListAdapter.LabViewHolder>() {

    /**
     * ViewHolder 클래스: 각 실험 아이템의 뷰를 담당
     */
    inner class LabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 각 뷰 컴포넌트 연결
        private val tvLabAlias: TextView = itemView.findViewById(R.id.tv_lab_alias)
        private val tvLabId: TextView = itemView.findViewById(R.id.tv_lab_id)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tv_created_at)
        private val tvBeaconCount: TextView = itemView.findViewById(R.id.tv_beacon_count)

        /**
         * Lab 데이터를 뷰에 바인딩
         */
        fun bind(lab: Lab) {
            // 실험명 (alias) 설정
            tvLabAlias.text = lab.alias

            // 실험 ID 설정
            tvLabId.text = "Lab ID: ${lab.labID}"

            // 생성 날짜 포맷팅 후 설정
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(lab.createdAt))
            tvCreatedAt.text = formattedDate

            // 비콘 개수 정보 설정 (Library의 Lab 엔티티에서 beaconPositions 사용)
            val beaconCount = lab.beaconPositions.size
            tvBeaconCount.text = "비콘: ${beaconCount}개"

            // 아이템 클릭 이벤트 설정 - ResultActivity로 이동
            itemView.setOnClickListener {
                onItemClick(lab)
            }
        }
    }

    /**
     * ViewHolder 생성
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lab, parent, false)
        return LabViewHolder(view)
    }

    /**
     * ViewHolder에 Lab 데이터 바인딩
     */
    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        holder.bind(labList[position])
    }

    /**
     * 아이템 개수 반환
     */
    override fun getItemCount(): Int = labList.size
}
