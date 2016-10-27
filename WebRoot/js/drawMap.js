/**
 * Created by win7 on 2016/10/8.
 */
var predictionCurveArrs = [];//预测线
var realCurveArrs = [];//实际线

var circleDataArr = [
    [{longitude:121.490468 ,latitude:31.233705450000002,name:"南京东路",color:"rgba(255,108,0,0.5)"}],
    [{longitude:121.45144272 ,latitude:31.19895475,name:"徐家汇",color:"rgba(96,96,99,0.5)"}],
    [{longitude:121.37339216,latitude:31.12945335,name:"莘庄",color:"rgba(156,0,255,0.5)"}]
];

var width  = $(window).width()*0.5;
var height = $(window).height();
var appendSvg = d3.select("#svgMap")
    .attr("width", width)
    .attr("height", height);
//    地图放大缩小函数
var zoom = d3.behavior.zoom()
    .scaleExtent([1, 100])
    .on("zoom", zoomed);
function zoomed() {
    d3.select(this).attr("transform",
        "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

var svg = appendSvg
    .append("g")
    .call(zoom)
    .attr("transform", "translate(-200,0)");
//绘制地图中心
var projection = d3.geo.mercator()
    .center([121.29, 31.14])
    .scale(46500)
    .translate([width/2, height/2]);
//   地图路径
var path = d3.geo.path()
    .projection(projection);

//通过经纬度获取，地图上的坐标位置
var getCoordinate=function(longitude, latitude){
    var coordinateArr =  projection([longitude, latitude]);
    return{
        x:coordinateArr[0],
        y:coordinateArr[1]
    }
};
$(document).ready(function() {
    $('.timePoint').jRange({
        from:0,
        to: 23,
        step: 1,
        scale: [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23],
        format: '%s',
        width: 400,
        showLabels: true,
        isRange : true
    });
    $('.hour_predict').jRange({
        from:0,
        to: 23,
        step: 1,
        scale: [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23],
        format: '%s',
        width: 400,
        showLabels: true,
        isRange : true
    });
    $("#datePicker").AnyPicker({
            mode: "datetime",
            dateTimeFormat: "yyyy-MM-dd",
            minValue: new Date(2016, 02, 01),
            maxValue: new Date(2016, 02, 31),
            onSetOutput: function(sOutput, oSelectedValues)
            {
                var date_input =  document.getElementById('datePicker').value;
                    DATE_LAYOUT_STATIC_GRID = date_input.split("-")[0]+date_input.split("-")[1]+date_input.split("-")[2];
                    var typeVal = $("#selectType").find("option:selected").val();
                    if(typeVal!=""){
                        getStaticGridUrl = "/soda-web/getClassLineServlet?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea+"&classType="+typeVal;
                    }else {
                        getStaticGridUrl ="/soda-web/getGridFromToNum2?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea;
                    }
                    drawPointOrLine()
            }
        });
    $("#date_predict").AnyPicker({
            mode: "datetime",
            dateTimeFormat: "yyyy-MM-dd",
            minValue: new Date(2016, 03, 01),
            maxValue: new Date(2016, 03, 10),
            onSetOutput: function(sOutput, oSelectedValues)
            {
                var date_predict =  document.getElementById('date_predict').value;
                DATE_PREDICT = date_predict.split("-")[0]+date_predict.split("-")[1]+date_predict.split("-")[2];
                var typeVal = $("#selectType_predict").find("option:selected").val();
                if(typeVal!=""){
                    getPredictServlet_1 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1&type="+typeVal;
                    getPredictServlet_2 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2&type="+typeVal;
                    getPredictServlet_3 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3&type="+typeVal;
                }else {
                    getPredictServlet_1 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1";
                    getPredictServlet_2 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2";
                    getPredictServlet_3 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3";
                }
                //getPredictServlet = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea="+tradingArea;
                getPredictServletFun();
            }
        });
    $("#submit").bind("click",function(){
    var val =  $(".timePoint").val();
    FROM_HOUR = val.split(",")[0];
    TO_HOUR = val.split(",")[1];
    var typeVal = $("#selectType").find("option:selected").val();
    if(typeVal!=""){
        getStaticGridUrl = "/soda-web/getClassLineServlet?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea+"&classType="+typeVal;
    }else {
        getStaticGridUrl ="/soda-web/getGridFromToNum2?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea;
    }
    drawPointOrLine()
});
    $("#submit_predict").bind("click",function(){
    var val =  $(".hour_predict").val();
        FROM_HOUR_PREDICT = val.split(",")[0];
        TO_HOUR_PREDICT = val.split(",")[1];
        var typeVal = $("#selectType_predict").find("option:selected").val();
        if(typeVal!=""){
            getPredictServlet_1 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1&type="+typeVal;
            getPredictServlet_2 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2&type="+typeVal;
            getPredictServlet_3 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3&type="+typeVal;
        }else {
            getPredictServlet_1 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1";
            getPredictServlet_2 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2";
            getPredictServlet_3 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3";
        }
        //getPredictServlet = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea="+tradingArea;
        getPredictServletFun();
});
    $("#selectType").bind("change",function(){
        var typeVal = $("#selectType").find("option:selected").val();
        if(typeVal!=""){
            getStaticGridUrl = "/soda-web/getClassLineServlet?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea+"&classType="+typeVal;
        }else {
            getStaticGridUrl ="/soda-web/getGridFromToNum2?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea;
        }
        drawPointOrLine();
    })
    $("#selectType_predict").bind("change",function(){
        var typeVal = $("#selectType_predict").find("option:selected").val();
        if(typeVal!=""){
            getPredictServlet_1 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1&type="+typeVal;
            getPredictServlet_2 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2&type="+typeVal;
            getPredictServlet_3 = "/soda-web/getPredictManTypeServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3&type="+typeVal;
        }else {
            getPredictServlet_1 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1";
            getPredictServlet_2 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2";
            getPredictServlet_3 ="/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3";
        }
        getPredictServletFun();
    })
});
document.getElementById('datePicker').value = "2016-03-01";
document.getElementById('date_predict').value = "2016-04-01";
var date_input = $("#datePicker").val();
var date_predict = $("#date_predict").val();
var DATE_LAYOUT_STATIC_GRID = date_input.split("-")[0]+date_input.split("-")[1]+date_input.split("-")[2];
var DATE_PREDICT = date_predict.split("-")[0]+date_predict.split("-")[1]+date_predict.split("-")[2];
var _Date = new Date();
var HOUR_LAYOUT_STATIC_GRID = _Date.getHours();//获取静态点数据时间
var FROM_HOUR = HOUR_LAYOUT_STATIC_GRID;
var TO_HOUR = HOUR_LAYOUT_STATIC_GRID;
var FROM_HOUR_PREDICT = HOUR_LAYOUT_STATIC_GRID;
var TO_HOUR_PREDICT = HOUR_LAYOUT_STATIC_GRID;
var tradingArea = 1;
var getStaticGridUrl ="/soda-web/getGridFromToNum2?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea;//获取静态点数据URL
var getPredictServlet_1 = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=1";
var getPredictServlet_2 = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=2";
var getPredictServlet_3 = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea=3";
getPredictServletFun();
function getPredictServletFun(){
    $.ajax({
        url:getPredictServlet_1,
        type:"get",
        dataType:"json",
        async: false,
        success:function(data){
            var text = "",htext = "",address = "";
            if(FROM_HOUR_PREDICT==TO_HOUR_PREDICT){
                htext = "的"+FROM_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }else {
                htext = "从"+FROM_HOUR_PREDICT+"点到"+TO_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }
            address = "南京东路";
            text =  address+DATE_PREDICT+htext;
            $(".predict_people_count").text(text)
        }
    });
    $.ajax({
        url:getPredictServlet_2,
        type:"get",
        dataType:"json",
        async: false,
        success:function(data){
            var text = "",htext = "",address = "";
            if(FROM_HOUR_PREDICT==TO_HOUR_PREDICT){
                htext = "的"+FROM_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }else {
                htext = "从"+FROM_HOUR_PREDICT+"点到"+TO_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }
                address = "徐家汇";
            text =  address+DATE_PREDICT+htext;
            $(".predict_people_count_xujiahui").text(text)
        }
    });
    $.ajax({
        url:getPredictServlet_3,
        type:"get",
        dataType:"json",
        async: false,
        success:function(data){
            var text = "",htext = "",address = "";
            if(FROM_HOUR_PREDICT==TO_HOUR_PREDICT){
                htext = "的"+FROM_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }else {
                htext = "从"+FROM_HOUR_PREDICT+"点到"+TO_HOUR_PREDICT+"点的预测人数为"+data.dataList[0].count+"人"
            }
                address = "莘庄";
            text =  address+DATE_PREDICT+htext;
            $(".predict_people_count_xinzhuang").text(text)
        }
    });
}
$(".timePoint").val(FROM_HOUR+","+TO_HOUR);
$(".hour_predict").val(FROM_HOUR+","+TO_HOUR);
var echart ={
    pie:  function (data){
        var myChart = echarts.init(document.getElementById('pie'));
        // 指定图表的配置项和数据
       var  option = {
            title : {
                text: '',
                subtext: '',
                x:'left'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
                legend: {
                    x : 'center',
                    y : 'bottom',
                    data:['出租车','公交','地铁','公交->地铁','地铁->公交',"其他"]
            },
            toolbox: {
                show : true,
                feature : {
                    mark : {show: true},
                    dataView : {show: false, readOnly: false},
                    magicType : {
                        show: true,
                        type: ['pie', 'funnel']
                    },
                    restore : {show: false},
                    saveAsImage : {show: false}
                }
            },
            calculable : false,
            series : [
                {
                    name:'搭乘人数',
                    type:'pie',
                    radius : [20, 130],
                    center : ['50%', '50%'],
                    roseType : 'radius',
                    label: {
                        normal: {
                            show: false
                        },
                        emphasis: {
                            show: true
                        }
                    },
                    lableLine: {
                        normal: {
                            show: false
                        },
                        emphasis: {
                            show: true
                        }
                    },
                    data:data
                }
            ]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
    } ,
    bar:  function (dataX,dataY){
        var myChart = echarts.init(document.getElementById('bar'));

        // 指定图表的配置项和数据
        var option = {
            title: {
                text: '',
                subtext: '单位（人）'
            },
            color: ['#3398DB'],
            tooltip : {
                trigger: 'axis',
                axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                    type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis : [
                {
                    type : 'category',
                    data : dataX,
                    axisTick: {
                        alignWithLabel: false
                    }
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
                {
                    name:'当前人数',
                    type:'bar',
                    coordinateSystem:"cartesian2d",
                    barWidth: '60%',
                    data:dataY,
                    markPoint:"triangle",
                    animationDelayUpdate: function (idx) {
                        // 越往后的数据延迟越大
                        return idx * 100;
                    }
                }
            ]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
    }
};
drawTipsLine();
function drawTipsLine(){
        appendSvg.selectAll(".rect").data(circleDataArr).enter()
            .append("rect")
            .attr("class","rect")
            .attr("x",width-150)
            .attr("y",function(d,i){
                var height = 40;
                height = height+i*22;
                return height
            })
            .attr("rx",5)
            .attr("ry",5)
            .attr("height",10)
            .attr("width",30)
            .attr("fill",function(d,i){
                if(i==0){
                   return  d[0].color
                }else {
                    return "gray"
                }

            })
            .on("click",function(d,i){
                var self =  d3.select(this);
                var cColor = self.attr("fill");
                if(cColor=="gray"){
                    d3.selectAll(".rect")
                        .attr("fill","gray");
                    self.attr("fill",function(){
                        return  d[0].color
                    });
                    tradingArea = i+1;
                    getStaticGridUrl ="/soda-web/getGridFromToNum2?date="+DATE_LAYOUT_STATIC_GRID+"&fromHour="+FROM_HOUR+"&toHour="+TO_HOUR+"&tradingArea="+tradingArea;
                    //getPredictServlet = "/soda-web/getPredictServlet?date="+DATE_PREDICT+"&fromHour="+FROM_HOUR_PREDICT+"&toHour="+TO_HOUR_PREDICT+"&tradingArea="+tradingArea;
                    drawPointOrLine();
                    //getPredictServletFun()
                }
            });

        appendSvg.selectAll(".text").data(circleDataArr).enter()
            .append("text")
            .attr("class","text")
            .attr("x",width-110)
            .attr("y",function(d,i){
                var height = 50;
                height = height+i*22;
                return height
            })
            .style('fill', 'white')
            .style('font-size', '10px')
            .style('font-family', '微软雅黑')
            .text(function(d){
                return d[0].name;
            });

    ////    虚实线表示
    //var Mx = width-150,
    //    Lx = width-90;
    //var tipsRealLine = "M"+Mx+" 20"+"L"+Lx +" 20";
    //var tipsPredictionLine = "M"+Mx+" 40"+"L"+Lx +" 40";
    //appendSvg.append("path")
    //    .attr("d",tipsRealLine)
    //    .attr("stroke","red")
    //    .attr("stroke-width",1)
    //    .attr("stroke-dashoffset",20)
    //    .attr("fill","none");
    //appendSvg.append("text")
    //    .attr("class","tipsText")
    //    .attr("x",Lx+20)
    //    .attr("y",25)
    //    .style('fill', 'white')
    //    .style('font-size', '10px')
    //    .style('font-family', '微软雅黑')
    //    .text("实际")
    //    .on("click",function(){
    //        if(realCurveArrs.length==0){
    //            d3.select(this).style("fill","white");
    //            getData.getRealData()
    //        }else {
    //            d3.select(this).style("fill","gray");
    //            realCurveArrs = [];
    //            drawRealLine()
    //        }
    //    });
    //
    //appendSvg.append("path")
    //    .attr("d",tipsPredictionLine)
    //    .attr("stroke","blue")
    //    .attr("stroke-width",1)
    //    .attr("stroke-dasharray","5,5")
    //    .attr("stroke-dashoffset",20)
    //    .attr("fill","none");
    //
    //
    //appendSvg.append("text")
    //    .attr("class","tipsText")
    //    .attr("x",Lx+20)
    //    .attr("y",45)
    //    .style('fill', 'white')
    //    .style('font-size', '10px')
    //    .style('font-family', '微软雅黑')
    //    .text("预测")
    //    .on("click",function(){
    //        if(predictionCurveArrs.length==0){
    //            d3.select(this).style('fill', 'white');
    //            getData.getPredictionData()
    //        }else {
    //            d3.select(this).style('fill', 'gray');
    //            predictionCurveArrs = [];
    //            drawPredictionLine()
    //        }
    //    });
}
drawTipCircle();
function  drawTipCircle(){
    var circleArrs = [500,3000,5500,55500];
    appendSvg.selectAll(".tipCircle").data(circleArrs).enter()
        .append("circle")
        .attr("class","tipCircle")
        .attr("cx",width-160)
        .attr("cy",function(d,i){
            return height-30*(i+1)
        })
        .attr("r",function(d){
            return getRadiusAndColor(d).r
        })
        .attr("fill",function(d){
            return getRadiusAndColor(d).c
        });

    appendSvg.selectAll(".circleText").data(circleArrs).enter()
        .append("text")
        .attr("class","circleText")
        .attr("x",width-150)
        .attr("y",function(d,i){
            return height-30*(i+1)+3
        })
        .style('fill', 'white')
        .style('font-size', '10px')
        .style('font-family', '微软雅黑')
        .text(function(d){
            var text = "";
            if(d>10000){
                text = "大于10000人"
            }else if(d>5000&&d<=10000){
                text = "小于等于10000大于5000人"
            }else if(d>2000&&d<=5000){
              text = "小于等于5000大于2000人"
            }else {
                text = "小于等于2000人"
            }
            return text;
        });
}
drawMap();
function  drawMap(){
    //绘制地图
    d3.json("../shanghai.json", function(error, root) {
        if (error)
            return console.error(error);
//            地图
        svg.selectAll("path")
            .data(root.features)
            .enter()
            .append("path")
            .attr("stroke", "#31f652")
            .attr("stroke-width", 0.5)
            .attr("fill", function (d, i) {
                return "#000";
            })
            .attr("d", path);
//地图文字
        svg.selectAll("text")
            .data(root.features)
            .enter()
            .append("text")
            .attr("x", function (d) {
                var coordinate = getCoordinate(d.properties.cp[0], d.properties.cp[1]);
                return coordinate.x
            })
            .attr("y", function (d) {
                var coordinate = getCoordinate(d.properties.cp[0], d.properties.cp[1]);
                return coordinate.y
            })
            .style('fill', '#7fc7ff')
            .style('font-size', '8px')
            .style('font-family', '微软雅黑')
            .text(function (d) {
                return d.properties.name
            });


        svg.selectAll(".group")
            .data(circleDataArr)
            .enter()
            .append("circle")
            .attr("class","group")
            .attr("cx",function(d,i){
                var coordinate = getCoordinate(d[0].longitude,d[0].latitude);
                return coordinate.x
            }).attr("cy",function(d,i){
                var coordinate = getCoordinate(d[0].longitude,d[0].latitude);
                return coordinate.y
            })
            .attr("r","15")
            .attr("fill",function(d,i){
                  return d[0].color
            });
            //.on("mouseover",function(d,i){
            //    var y = d3.select(this).attr("cy");
            //    var r = d3.select(this).attr("r");
            //    var x = d3.select(this).attr("cx") * 1 + 1 * r;
            //    x = parseFloat(x)-200;
            //    y = parseFloat(y)+80;
            //    var tooltip = d3.select("#tooltip")
            //        .style("left", x + "px")
            //        .style("top", y + "px")
            //        .style("display","block")
            //    tooltip.select("#x").text(d[0]);
            //    tooltip.select("#y").text(d[1]);
            //    tooltip.select("#name").text(d[2]);
            //})
            //.on("mouseout",function(d,i){
            //    var tooltip = d3.select("#tooltip")
            //        .style("display","none")
            //})
            //.on("click",function(d){
            //    dataset = d[0].dataset;
            //    labelText = d[0].labelText;
            //    $("#iframe").attr("src","bar.html?dataset="+dataset+"&labelText="+labelText+"&name="+d[0].name)
            //});


        //商圈文字
        svg.selectAll(".circleText")
            .data(circleDataArr)
            .enter()
            .append("text")
            .attr("class","circleText")
            .attr("x",function(d,i){
                var coordinate = getCoordinate(d[0].longitude,d[0].latitude);
                return coordinate.x
            }).attr("y",function(d,i){
                var coordinate = getCoordinate(d[0].longitude,d[0].latitude);
                return coordinate.y
            })
            .style('font-size', '10px')
            .style('font-family', '微软雅黑')
            .attr("fill","#ffd700")
            .text(function(d){
                return d[0].name;
            });
    });


}
drawGrid();
function drawGrid(){
    d3.json("/soda-web/getGridLine",function(error,root){
        //console.log(root,"root")
        svg.selectAll(".Xgrids")
            .data(root.xLine)
            .enter()
            .append("path")
            .attr("class","Xgrids")
            .attr("d",function(d){
                var startCoordinate = getCoordinate(d[0].x, d[0].y);
                var endCoordinate = getCoordinate(d[1].x, d[1].y);
                return "M"+startCoordinate.x+" "+startCoordinate.y +"L"+endCoordinate.x+" "+endCoordinate.y
            })
            .attr("stroke","#333")
            .attr("stroke-width",1)
            .attr("stroke-dasharray","5,5")
            .attr("stroke-dashoffset",20)
            .attr("fill","none");

//Y轴
        svg.selectAll(".Ygrids")
            .data(root.yLine)
            .enter()
            .append("path")
            .attr("class","Ygrids")
            .attr("d",function(d){
                var startCoordinate = getCoordinate(d[0].x, d[0].y);
                var endCoordinate = getCoordinate(d[1].x, d[1].y);
                return "M"+startCoordinate.x+" "+startCoordinate.y +"L"+endCoordinate.x+" "+endCoordinate.y
            })
            .attr("stroke","#333")
            .attr("stroke-width",1)
            .attr("stroke-dasharray","5,5")
            .attr("stroke-dashoffset",20)
            .attr("fill","none");
    })
}
function line(d){
        var startCoordinate = getCoordinate(d.from_longitude, d.from_latitude);
        var endCoordinate = getCoordinate(d.to_longitude, d.to_latitude);
        var line = d3.svg.line()
            .x(function(d) { return d.x; })
            .y(function(d) { return d.y; })
            .interpolate("basis");

       var startX = "",startY = "",endX = "",endY = "";
        var num = parseInt(Math.random()*(25-1+1)+1,10);
        //startX = startCoordinate.x- d.hour;
        //startY = startCoordinate.y- d.hour;
        //endX = endCoordinate.x- d.hour;
        //endY = endCoordinate.y- d.hour;
        if(d.hour<=10){
            startX = startCoordinate.x- num;
            startY = startCoordinate.y- num;
            endX = endCoordinate.x- num;
            endY = endCoordinate.y- num
        }else {
            startX = startCoordinate.x+num;
            startY = startCoordinate.y+num;
            endX = endCoordinate.x+num;
            endY = endCoordinate.y+num
        }

        var dx = endX - startX,
            dy = endY -startY,
            dr = Math.sqrt(dx * dx + dy * dy);
        return "M" + startCoordinate.x + "," + startY + "A" + dr + "," + dr + " 0 0,1 " + endCoordinate.x + "," + endY;
        //return "M"+startCoordinate.x+" "+startCoordinate.y +" Q"+x0+","+y0+" "+endCoordinate.x+" "+endCoordinate.y
    }
    //drawPredictionLine();
var colorArr = ["#ffafaf","#ff5454","#ffa030","#ffe6af","#ffea00","#ff6c00","#05ff28","#4dfff9","#2e8217","#f54fff","#2263d9",
    "#9c00ff","#8d0000","#8f00a4","#e5ff6b","#6d3e2c","#18d091","#0011ea","#0a5d6f","#828b20","#ff7f94","#ff2828","#4000aa","#96671d"];
//预测虚线
function drawPredictionLine(root){
        //var defs = appendSvg.append("defs");
        //var arrowMarker = defs.append("marker")
        //    .attr("id","arrow1")
        //    .attr("markerUnits","strokeWidth")
        //    .attr("markerWidth","12")
        //    .attr("markerHeight","12")
        //    .attr("viewBox","0 0 12 12")
        //    .attr("refX","6")
        //    .attr("refY","6")
        //    .attr("orient","auto");
        //var arrow_path = "M2,2 L10,6 L2,10 L6,6 L2,2";
        //arrowMarker.append("path")
        //    .attr("d",arrow_path)
        //    .attr("fill","blue")

    svg.append("defs").selectAll("marker")
        .data(["arrow0", "arrow1", "arrow2","arrow3","arrow4","arrow5","arrow6","arrow7","arrow8","arrow9","arrow10",
            "arrow11","arrow12","arrow13","arrow14","arrow15","arrow16","arrow17","arrow18","arrow19","arrow20","arrow21","arrow22","arrow23"])
        .enter().append("marker")
        .attr("id", function(d) { return d; })
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -1.5)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5");

        var updateCurve = svg.selectAll(".curve")
            .data(root);
        var enterCurve = updateCurve.enter();
        var exitCurve = updateCurve.exit();
        updateCurve .attr("d",function(d){
                var startCoordinate = getCoordinate(d.from_longitude, d.from_latitude);
                return "M"+startCoordinate.x+" "+startCoordinate.y +"L"+startCoordinate.x+" "+startCoordinate.y
            })
            .attr("stroke",function(d){
                return colorArr[d.hour]
            })
            .attr("stroke-width",1)
            .attr("stroke-dasharray","5,5")
            .attr("stroke-dashoffset",20)
            .attr("fill","none")
            .attr("marker-end",function(d) { return "url(#arrow" + d.hour + ")"; })
            .transition()
            .duration(1500)
            .attr("d",line);
        enterCurve.append("path")
            .attr("class","curve")
            .attr("d",function(d){
                var startCoordinate = getCoordinate(d.from_longitude, d.from_latitude);
                return "M"+startCoordinate.x+" "+startCoordinate.y +"L"+startCoordinate.x+" "+startCoordinate.y
            })
            .attr("stroke",function(d){
                return colorArr[d.hour]
            })
            .attr("stroke-width",1)
            .attr("stroke-dasharray","5,5")
            .attr("stroke-dashoffset",20)
            .attr("fill","none")
            .attr("marker-end",function(d) { return "url(#arrow" + d.hour + ")"; })
            //.on("mouseover",function(d,i){
            //    var startCoordinate = getCoordinate(d[0],d[1]);
            //    var x = parseFloat(startCoordinate.x)-50;
            //    var y = parseFloat(startCoordinate.y)-40;
            //    x = parseFloat(x)-200;
            //    y = parseFloat(y)+80;
            //    var tooltip = d3.select("#tooltip")
            //        .style("left", x + "px")
            //        .style("top", y + "px")
            //        .style("display","block");
            //    tooltip.select("#x").text(d[0],d[1]);
            //    tooltip.select("#y").text(d[2],d[3]);
            //    tooltip.select("#name").text(d[4]);
            //})
            //.on("mouseout",function(d,i){
            //    var tooltip = d3.select("#tooltip")
            //        .style("display","none")
            //})
            .transition()
            .duration(1500)
            .attr("d",line);
        exitCurve.remove();


    }
//实线
function drawRealLine(root){
    svg.append("defs").selectAll("marker")
        .data(["arrow0", "arrow1", "arrow2","arrow3","arrow4","arrow5","arrow6","arrow7","arrow8","arrow9","arrow10",
            "arrow11","arrow12","arrow13","arrow14","arrow15","arrow16","arrow17","arrow18","arrow19","arrow20","arrow21","arrow22","arrow23"])
        .enter().append("marker")
        .attr("id", function(d) { return d; })
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -1.5)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5");
    //实线
    var realUpdateCurve = svg.selectAll(".realCurve")
        .data(root);
    //console.log(root)
    var realEnterCurve = realUpdateCurve.enter();
    var realExitCurve = realUpdateCurve.exit();
    realUpdateCurve .attr("d",function(d){
            var startCoordinate = getCoordinate(d.from_longitude, d.from_latitude);
            return "M"+startCoordinate.x+" "+startCoordinate.y+"L"+startCoordinate.x+" "+startCoordinate.y;
        })
        .attr("stroke",function(d){
            return colorArr[d.hour]
        })
        .attr("stroke-width",1)
        //.attr("stroke-dashoffset",20)
        .attr("fill","none")
        .transition()
        .duration(1500)
        .attr("d",line)
        .attr("marker-end",function(d) { return "url(#arrow" + d.hour + ")"; });
    realEnterCurve.append("path")
        .attr("class","realCurve")
        .attr("d",function(d){
            var startCoordinate = getCoordinate(d.from_longitude, d.from_latitude);
            return "M"+startCoordinate.x+" "+startCoordinate.y+"L"+startCoordinate.x+" "+startCoordinate.y;
        })
        .attr("stroke",function(d){
            return colorArr[d.hour]
        })
        .attr("stroke-width",1)
        //.attr("stroke-dashoffset",20)
        .attr("fill","none")
        .attr("marker-end",function(d) { return "url(#arrow" + d.hour + ")"; })
        .on("click",function(d,i){
            if(!d.type){
                $("#bar").css("display","inline-block");
                $.ajax({
                    url:"/soda-web/getGridPeopleGroup2?groupId="+ d.grid_people_group_id,
                    type:"get",
                    dataType:"json",
                    async: false,
                    success:function(data){
                        var dataX = [];
                        var dataY = [];
                        for(var i=0;i<data.dataList.length;i++){
                            dataX.push(data.dataList[i].type);
                            dataY.push(data.dataList[i].count);
                        }
                        echart.bar(dataX,dataY)
                    }
                })
            }else {
                $("#bar").css("display","none");
            }

        })
        //.on("mouseout",function(d,i){
        //    var tooltip = d3.select("#tooltip")
        //        .style("display","none")
        //})
        .transition()
        .duration(1500)
        .attr("d",line);
    realExitCurve.remove()
    }
function getRadiusAndColor(d){
    //var d = d;
    var r = 0;
    var c = "";
    var d = parseFloat(d);
    if(d>10000){
        r = 5;
        c = "#FF0000"
    }else if(d>5000&&d<=10000){
        r = 4;
        c = "#EEEE00"
    }else if(d>2000&&d<=5000){
        r = 3
        c = "#156dd0"
    }else {
        r = 2
        c = "#EE9572"
    }
    return {
        r:r,
        c:c
    }
}
//drawStatusPoint();
drawPointOrLine();
var timer;
function drawPointOrLine(){
    clearInterval(timer);
    d3.json(getStaticGridUrl,function (error,root){
        //console.log(root,"root")
        var drawLine = [];
        var drawPoint = [];
        var drawRect = [];
        var text = "",htext = "",address = "";
        var total = 0;
        if(root.total){
            total = root.total;
        }else {
            total = 0
        }
        if(FROM_HOUR==TO_HOUR){
            htext = "的"+FROM_HOUR+"点的实际人数为"+total+"人"
        }else {
            htext = "从"+FROM_HOUR+"点到"+TO_HOUR+"点的实际人数为"+total+"人"
        }
        if(tradingArea == 1){
            address = "南京东路";
        }else  if(tradingArea==2){
            address = "徐家汇";
        }else {
            address = "莘庄";
        }
        text =  address+DATE_LAYOUT_STATIC_GRID+htext;
        $(".people_count").text(text);
        if(root.ResultType=="static"){
            for (var i=0;i<root.dataList.length;i++){
                if(root.dataList[i].warn){
                    drawRect.push(root.dataList[i])
                }else {
                    drawPoint.push(root.dataList[i])
                }
            }
            //drawPoint = root.dataList;
           timer = setInterval(function(){
                drawStatusFlashingPoint(drawRect);
            },2000);
            drawStatusPoint(drawPoint);
            drawRealLine(drawLine);

            //drawPredictionLine(drawLine);
        }else {
            drawLine = root.dataList;
            drawStatusPoint(drawPoint);
            drawStatusFlashingPoint(drawRect);
            drawRealLine(drawLine);
            //drawPredictionLine(drawLine)
        }
        echart.pie(root.graphData);
    })
}
function drawStatusPoint(root){
    //静态圆点
    var staticGridUpdateCircle = svg.selectAll(".staticGrid")
        .data(root);
    var staticGridEnterCircle = staticGridUpdateCircle.enter();
    var staticGridExitCircle = staticGridUpdateCircle.exit();
    staticGridUpdateCircle. attr("cx",function(d,i){
        var coordinate = getCoordinate(d.longitude, d.latitude);
        return coordinate.x
    }).attr("cy",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.y
        })
        .attr("r",function(d){
            return getRadiusAndColor(d.count).r
        })
        .attr("fill",function(d){
            return getRadiusAndColor(d.count).c
        });

    staticGridEnterCircle.append("circle")
        .attr("class","staticGrid")
        .attr("cx",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.x
        }).attr("cy",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.y
        })
        .attr("r",function(d){
            //console.log(d.count)
            return getRadiusAndColor(d.count).r
        })
        .attr("fill",function(d){
            return getRadiusAndColor(d.count).c
        })
        .on("click",function(d,i){
    $("#bar").css("display","inline-block");
    $.ajax({
        url:"/soda-web/getGridPeopleGroup2?groupId="+ d.grid_people_group_id,
        type:"get",
        dataType:"json",
        async: false,
        success:function(data){
            var dataX = [];
            var dataY = [];
            for(var i=0;i<data.dataList.length;i++){
                dataX.push(data.dataList[i].type);
                dataY.push(data.dataList[i].count);
            }
            echart.bar(dataX,dataY)
        }
    })
        })
        .on("mouseover",function(d,i){
             d3.select(this)
                 .transition()
                 .duration(200)
                 .ease("linear")
                 .attr("r",7)
                 .attr("fill","#1ad015");
            var tooltip = d3.select("#tooltip")
                .style("left", (d3.event.pageX+15) + "px")
                .style("top", (d3.event.pageY+15) + "px")
                .style("display","block");
            tooltip.select("#total_p").text(d.count);
        })
        .on("mouseout",function(d,i){
            d3.select(this)
                .transition()
                .duration(200)
                .ease("linear")
                .attr("r",function(d){
                    return getRadiusAndColor(d.count).r
                })
                .attr("fill",function(d){
                    return getRadiusAndColor(d.count).c
                });
            var tooltip = d3.select("#tooltip")
                .style("display","none")
        });
    staticGridExitCircle.remove();
    }
function drawStatusFlashingPoint(root){
    //静态圆点
    var staticGridUpdateCircle = svg.selectAll(".flashPoint")
        .data(root);
    var staticGridEnterCircle = staticGridUpdateCircle.enter();
    var staticGridExitCircle = staticGridUpdateCircle.exit();
    staticGridUpdateCircle. attr("cx",function(d,i){
        var coordinate = getCoordinate(d.longitude, d.latitude);
        return coordinate.x
    }).attr("cy",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.y
        })
        .attr("fill",function(d){
            return "#ff2828"
        })
        .transition('size')
        .attr('r', 0)
        .duration(1000)

        .transition('size')
        .attr('r', 7)
        .duration(1000);

    staticGridEnterCircle.append("circle")
        .attr("class","flashPoint")
        .attr("cx",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.x
        }).attr("cy",function(d,i){
            var coordinate = getCoordinate(d.longitude, d.latitude);
            return coordinate.y
        })
        .attr("fill",function(d){
            return "#ff2828"
        })
        .on("click",function(d,i){
            $("#bar").css("display","inline-block");
            $.ajax({
                url:"/soda-web/getGridPeopleGroup2?groupId="+ d.grid_people_group_id,
                type:"get",
                dataType:"json",
                async: false,
                success:function(data){
                    var dataX = [];
                    var dataY = [];
                    for(var i=0;i<data.dataList.length;i++){
                        dataX.push(data.dataList[i].type);
                        dataY.push(data.dataList[i].count);
                    }
                    echart.bar(dataX,dataY)
                }
            })
        })
        .on("mouseover",function(d,i){
            //d3.select(this)
            //    .transition()
            //    .duration(200)
            //    .ease("linear")
            //    .attr("r",7)
            //    .attr("fill","#6d3e2c");
            var tooltip = d3.select("#tooltip")
                .style("left", (d3.event.pageX+15) + "px")
                .style("top", (d3.event.pageY+15) + "px")
                .style("display","block");
            tooltip.select("#total_p").text(d.count);
        })
        .on("mouseout",function(d,i){
            //d3.select(this)
            //    .transition()
            //    .duration(200)
            //    .ease("linear")
            //    .attr("r",function(d){
            //        return getRadiusAndColor(d.count).r
            //    })
            //    .attr("fill",function(d){
            //        return getRadiusAndColor(d.count).c
            //    });
            var tooltip = d3.select("#tooltip")
                .style("display","none")
        })

        .transition('size')
        .attr('r', 0)
        .duration(1000)

        .transition('size')
        .attr('r', 5)
        .duration(1000);
    staticGridExitCircle.remove();
}




