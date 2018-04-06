package com.dan.stickynote;

public class Task {

    private  String name;
    private  int imageId;
    private String deadline;

    public Task(String name ,int imageId, String deadline)
    {
        this.name=name;
        this.imageId=imageId;
        this.deadline = deadline;
    }
    public String getName(){  return  name; }

    public int getImageId(){
        return  imageId;
    }

    public void setName(String s){
        this.name = s;
    }

    public void setImageId(int id){
        this.imageId = id;
    }

    public void setDeadline(String deadline)  { this.deadline = deadline; }

    public String getDeadline()  {  return deadline;  }

}
