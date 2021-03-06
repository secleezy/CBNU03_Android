package com.example.cbnu_03_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    TextView nameTextView, purposeTextView, leaderTextView, numberTextView;
    Button createGroupBtn, exitGroupBtn, findGroupBtn;
    String loginUser;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    ArrayList<User> userList;
    private DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        db = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        loginUser = intent.getStringExtra("userName");

        nameTextView = (TextView)findViewById(R.id.NameTextView);
        purposeTextView = (TextView)findViewById(R.id.purposeTextView);
        leaderTextView = (TextView)findViewById(R.id.leaderTextView);
        numberTextView = (TextView)findViewById(R.id.numberTextView);

        createGroupBtn = (Button)findViewById(R.id.createGroupBtn);
        exitGroupBtn = (Button)findViewById(R.id.exitGroupBtn);
        findGroupBtn = (Button)findViewById(R.id.findGroupBtn);


        //??????????????? ?????????.
        exitGroupBtn.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.groupRecyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        userList = new ArrayList<User>();

        recyclerAdapter = new RecyclerAdapter(userList);
        recyclerView.setAdapter(recyclerAdapter);

        db.child("userList").child(loginUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                String selectedGroup = user.getGroup();

                if(selectedGroup != null) {

                    //????????? ????????? ?????????..
                    //?????? ????????????

                    exitGroupBtn.setVisibility(View.VISIBLE);
                    db.child("groupList").child(selectedGroup).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            recyclerAdapter.clearList();
                          Group group = snapshot.getValue(Group.class);

                          ArrayList<String> userList = group.getUserArrayList();

                          //username ???????????? db?????? User ??????

                          for(String userName : userList){

                              db.child("userList").child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                                      User user = snapshot.getValue(User.class);
                                      recyclerAdapter.addItem(user);
                                      recyclerAdapter.notifyDataSetChanged();
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {

                                  }
                              });
                          }

                            //????????? ??????
                          nameTextView.setText("?????????: " + group.getName());
                          purposeTextView.setText("??????: " + group.getPurpose());
                          leaderTextView.setText("??????: " + group.getLeader());
                          numberTextView.setText("?????? ???: " + group.userArrayList.size());
                          createGroupBtn.setVisibility(View.INVISIBLE);
                          findGroupBtn.setVisibility(View.INVISIBLE);



                        }

                        //????????? ???
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(getApplicationContext(), CreateGroupActivity.class);
                newIntent.putExtra("userName", loginUser);
                startActivity(newIntent);
            }
        });

        exitGroupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                db.child("userList").child(loginUser).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        String searchGroup = user.getGroup();
                        user.setGroup(null);
                        db.child("userList").child(loginUser).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                createGroupBtn.setVisibility(View.VISIBLE);
                                findGroupBtn.setVisibility(View.VISIBLE);
                                exitGroupBtn.setVisibility(View.INVISIBLE);
                                recyclerAdapter.clearList();

                                nameTextView.setText("????????? ??????????????????!");
                                purposeTextView.setText(" ");
                                leaderTextView.setText(" ");
                                numberTextView.setText(" ");


                            }
                        });

                        //?????? ????????????????????? ??????.

                        db.child("groupList").child(searchGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group selectedGroup = snapshot.getValue(Group.class);

                                //?????? ???????????? ????????? ?????? ?????? ????????????.
                                if(selectedGroup.userArrayList.size() == 1){
                                    db.child("groupList").child(selectedGroup.getName()).removeValue();
                                }else{

                                    selectedGroup.userArrayList.remove(loginUser);
                                    //??????.. ????????? ????????? ?????????????
                                    if(selectedGroup.getLeader().equals(loginUser)){
                                        //????????? ???????????? ?????? ??????.
                                        selectedGroup.setLeader(selectedGroup.userArrayList.get(0));
                                    }

                                    db.child("groupList").child(searchGroup).setValue(selectedGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        findGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent newIntent = new Intent(getApplicationContext(), FindGroupActivity.class);
                newIntent.putExtra("userName", loginUser);
                startActivity(newIntent);

            }
        });



    }

    //RecyclerAdapter
    class RecyclerAdapter extends RecyclerView.Adapter<GroupActivity.RecyclerAdapter.ItemViewHolder>{

        private List<User> userList;

        public RecyclerAdapter(List<User> userList){
            this.userList = userList;
        }

        @NonNull
        @Override
        public GroupActivity.RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
            boolean attachToRoot; // !
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_user, viewGroup, attachToRoot = false); // !
            return new RecyclerAdapter.ItemViewHolder(view);
        }

        @Override
        public int getItemCount(){
            return userList.size();
        }

        //onBindViewHolder : ???????????? ??????????????? ????????? ???????????? ??????
        @Override
        public void onBindViewHolder(@NonNull GroupActivity.RecyclerAdapter.ItemViewHolder itemViewHolder, int i){
            User user = userList.get(i);


            //?????????????????? Memo??? ???????????????, ?????????????????? Memo??? ??????????????? ??????
            itemViewHolder.listUserName.setText("????????????: "+ user.getName());
            itemViewHolder.listUserContact.setText(user.getPhoneNumber());
            itemViewHolder.listUserId.setText("??????ID: " + user.getId());

        }

        //????????? ??????, ??????
        void addItem(User user){
            userList.add(user);
        }

        class ItemViewHolder extends RecyclerView.ViewHolder{

            private TextView listUserName;
            private TextView listUserContact;
            private TextView listUserId;
            private ImageView img;

            public ItemViewHolder(@NonNull View itemView){
                super(itemView);

                listUserName = itemView.findViewById(R.id.listUserName);
                listUserContact = itemView.findViewById(R.id.listUserContact);
                listUserId = itemView.findViewById(R.id.listUserId);
                leaderTextView = (TextView)findViewById(R.id.leaderTextView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //gropleader??? ???????????? ??????, textview ??? ?????????????????? ????????? ??????.
                        //tokenize ????????? ????????? token??? ??????.
                        if(leaderTextView.getText().toString().split(" ")[1].equals(loginUser)){
                            int pos = getAdapterPosition();
                            if(pos != RecyclerView.NO_POSITION){

                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);

                                builder.setTitle("????????? ????????? ??????????????????????");

                                builder.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        int pos = getAdapterPosition();
                                        User user = userList.get(pos);
                                        String deleteUserName = user.getId();
                                        if (deleteUserName.equals(loginUser)) {
                                            Toast.makeText(getApplicationContext(), "??????????????? ????????? ??? ????????????. ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                            dialogInterface.cancel();
                                        } else {

                                            db.child("userList").child(deleteUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    User user = snapshot.getValue(User.class);
                                                    String searchGroup = user.getGroup();
                                                    String deleteUserName = user.getId();
                                                    user.setGroup(null);
                                                    db.child("userList").child(deleteUserName).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();

                                                        }
                                                    });

                                                    //?????? ????????????????????? ??????.

                                                    db.child("groupList").child(searchGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Group selectedGroup = snapshot.getValue(Group.class);

                                                            //?????? ???????????? ????????? ?????? ?????? ????????????.
                                                            if (selectedGroup.userArrayList.size() == 1) {
                                                                db.child("groupList").child(selectedGroup.getName()).removeValue();
                                                            } else {

                                                                selectedGroup.userArrayList.remove(deleteUserName);

                                                                db.child("groupList").child(searchGroup).setValue(selectedGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(getApplicationContext(), "?????????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }

                        });
                                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                    }
                }

                img = itemView.findViewById(R.id.item_image);

            }

        });
            }
        }

        public void clearList(){
            userList.clear();
        }
    }

}



