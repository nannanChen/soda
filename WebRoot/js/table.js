/**
 * Created by win7 on 2016/10/28.
 */
$(document).ready(function(){

    var groupId = GetParam("groupId");
    var type = GetParam("type");
    var url = "";
if(type){
    url = "/soda-web/getGridPeopleGroup2?groupId="+groupId+"&type="+type ;
}else {
    url = "/soda-web/getGridPeopleGroup2?groupId="+groupId;
}
    console.log(groupId);
    console.log(type);
    function GetParam(paramName) {
        var r = new RegExp(paramName + '=([^=&]+)', 'i');
        var mm = window.location.search.match(r);
        if (mm) {
            return mm[1];
        } else {
            return null;
        }
    }
    tableData();
    function tableData(){
        $.ajax({
            url:url,
            type:"get",
            dataType:"json",
            async: false,
            success:function(data){
                var tr1 = $(".tr-1");
                $(".table-tr").remove();
                for(var i=0;i<data.dataList.length;i++){
                    tr1.after("<tr class='table-tr'><td>"+data.dataList[i].count+"</td><td>"+data.dataList[i].IMEIS+"</td><td>"+data.dataList[i].type+"</td></tr>")
                }
            }
        });
    }
});