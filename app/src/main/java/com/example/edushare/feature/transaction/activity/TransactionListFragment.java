package com.example.edushare.feature.transaction.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Nhớ thêm import này để không bị lỗi đỏ TextView
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edushare.R;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.transaction.adapter.TransactionAdapter;
import com.example.edushare.feature.transaction.repository.TransactionRepository;
import com.example.edushare.feature.transaction.view_model.TransactionHistoryViewModel;

import java.util.ArrayList;

public class TransactionListFragment extends Fragment {
    private TextView tvEmptyState;     // Khai báo chữ báo trống
    private RecyclerView rvTransactions; // Dùng thống nhất 1 tên biến này thôi nhé ông
    private TransactionAdapter adapter;
    private String status;
    private String role = "BUYER";
    private TransactionHistoryViewModel viewModel;

    public static TransactionListFragment newInstance(String status, String role) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle args = new Bundle();
        args.putString("STATUS", status);
        args.putString("ROLE", role);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        // ĐÃ SỬA: Tìm Id và kết nối cả RecyclerView lẫn TextView báo trống từ XML vào đây
        rvTransactions = view.findViewById(R.id.rvTransactions);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString("STATUS");
            role = getArguments().getString("ROLE");
        }
        TransactionRepository repository = new TransactionRepository(requireContext());
        viewModel = new TransactionHistoryViewModel(repository);

        setupRecyclerView();
        setupObservers();

        String currentUserId = UserSessionHelper.getUserId(requireContext());
        String token = UserSessionHelper.getToken(requireContext());

        viewModel.fetchHistory(token, currentUserId, role, status);
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        String token = UserSessionHelper.getToken(requireContext());
        adapter = new TransactionAdapter(new ArrayList<>(), role, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onAcceptClick(int transactionId) {
                viewModel.acceptTransaction(token, transactionId);
            }

            @Override
            public void onRejectClick(int transactionId) {
                viewModel.rejectTransaction(token, transactionId);
            }
        });

        rvTransactions.setAdapter(adapter);
    }

    private void setupObservers() {
        // Lắng nghe danh sách trả về
        viewModel.getTransactionsLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                // Có data: Hiện danh sách, ẩn chữ báo trống
                rvTransactions.setVisibility(View.VISIBLE);
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

                adapter.updateData(list, this.role);
            } else {
                // Không có data hoặc rỗng: Ẩn danh sách, hiện chữ "Chưa có đơn hàng nào"
                rvTransactions.setVisibility(View.GONE);
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Chưa có đơn hàng nào");
                }
            }
        });
        viewModel.getActionSuccessLiveData().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Thao tác thành công!", Toast.LENGTH_SHORT).show();

                String currentUserId = UserSessionHelper.getUserId(requireContext());
                String token = UserSessionHelper.getToken(requireContext());
                viewModel.fetchHistory(token, currentUserId, role, status);
            }
        });
        // Lắng nghe loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Tạm thời để trống
        });

        // Lắng nghe lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            rvTransactions.setVisibility(View.GONE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Không thể tải dữ liệu: " + error);
            }
        });
    }
}