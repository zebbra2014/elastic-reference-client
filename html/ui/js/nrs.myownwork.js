/**
 * @depends {nrs.js}
 */
 function formatBytes(bytes,decimals) {
   if(bytes == 0) return '0 Byte';
   var k = 1000; // or 1024 for binary
   var dm = decimals + 1 || 3;
   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
   var i = Math.floor(Math.log(bytes) / Math.log(k));
   return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

var NRS = (function(NRS, $, undefined) {

	
	var newworkrow = "<a href='#' data-target='#new_work_modal' data-toggle='modal' class='list-group-item larger-sidebar-element grayadder'>";
		newworkrow += "<i class='fa fa-edit work-image-type fa-5x'></i><p class='composelabel'>Click here to compose a new job</p>"
		newworkrow += "</a>";

	var _work = {};
	var computation_power=[];
	var solution_rate=[];

	NRS.pages.myownwork = function(callback) {
		_work = [];
		$("#no_work_selected").show();
		$("#work_details").hide();
		$(".content.content-stretch:visible").width($(".page:visible").width());



		_work.sort(function(a, b) {
				if (a.time_created > b.time_created) {
					return 1;
				} else if (a.time_created < b.time_created) {
					return -1;
				} else {
					return 0;
				}
			});

		// TODO, FIXME! Add lazy loading, specially when users do A LOT OF work
		NRS.sendRequest("getAccountWork", {
			"account": NRS.account,
			"type": 1
		}, function(response) {
			if (response.work_packages && response.work_packages.length) {
				for (var i = 0; i < response.work_packages.length; i++) {
					_work.push(response.work_packages[i]);
				}
				displayWorkSidebar(callback);
			} else {
				$("#no_work_selected").show();
				$("#work_details").hide();
				$("#myownwork_sidebar").empty().append(newworkrow);;

				NRS.pageLoaded(callback);
			}
		});
	}
	function statusText(message){
		return "<b>" + message.balance_remained + "+</b> XEL left, <b>7742</b> blocks left"; 
	}
	function status2Text(message){
		return "<b>" + message.percent_done + "%</b> done"; 
	}
	function ETA(message){
		return "ETA <b><1.5h</b>"; 
	}
	function timeOut(message){
		var blocksLeft = parseInt(message.timeout_at_block);
		blocksLeft -= NRS.lastBlockHeight;

		if (blocksLeft > 500)
			blocksLeft=">500";
		if (blocksLeft <=0 )
			blocksLeft="0";


		return "<b>" + blocksLeft + "</b> blocks"; 
	}
	function efficiency(efficiency){
		return "<b>1%</b> efficiency"; 
	}
	function statusspan(message){
		return "<span class='label label-success label12px'>Active</span>";
	}
	function balancespan(message){
		return "<span class='label label-white label12px'>" + NRS.formatAmount(message.balance_remained) + " XEL</span>";
	}

	function flopsFormatter(v, axis) {
        return v.toFixed(axis.tickDecimals) + "G";
    }
    function perHourFormatter(v, axis) {
        return v.toFixed(axis.tickDecimals) + "/h  ";
    }

	function doPlot(){
	

		if(computation_power.length == 0 && solution_rate.length == 0){
			$("#flot_alert").removeClass("flot_hidden");
		}else{
			$("#flot_alert").addClass("flot_hidden");
		}
	 

	    $.plot($("#flot-line-chart-multi"), [{
	        data: computation_power,
	        label: "Computation Power in Flops"
	    }, {
	        data: solution_rate,
	        label: "  Generation Rate of Useful Solutions per Hour",
	        yaxis: 2
	    }], {
	        xaxes: [{
	            mode: 'time',timeformat: "%H:%M"
	        }],
	        yaxes: [{
	            min: 0,tickFormatter: perHourFormatter
	        }, {
	            // align if we are to the right
	            alignTicksWithAxis: 1,
	            position: "right",
	            tickFormatter: flopsFormatter
	        }],
	        legend: {
	            position: 'sw'
	        }
	    });
    
	}

	function displayWorkSidebar(callback) {
		console.log("mywork callback fired!");
		var activeAccount = false;

		var $active = $("#myownwork_sidebar a.active");

		if ($active.length) {
			activeAccount = $active.data("account");
		}

		var rows = "";
		var menu = "";

		// Here, add the NEW WORK row
		rows += newworkrow;
		for (var i = 0; i < _work.length; i++) {
			var message = _work[i];
			rows += "<a href='#' class='list-group-item larger-sidebar-element selectable' data-array-index='" + i + "'><p class='list-group-item-text agopullright'>" + balancespan(message) + " " + statusspan(message) + " <span class='label label-primary label12px'>" + message.language + "</span></p><span class='list-group-item-heading betterh4'>" + message.title + "</span><br><small>created 1 day ago (block #13318)</small><span class='middletext_list'>" + /* BEGIN GRID */ "<div class='row fourtwenty'><div class='col-md-3'><i class='fa fa-tasks fa-fw'></i> " + status2Text(message) + "</div><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-3'><i class='fa fa-times-circle-o fa-fw'></i> " + timeOut(message) + "</div><div class='col-md-3'><i class='fa fa-rocket fa-fw'></i> " + efficiency(message) + "</div></div>" /* END GRID */ + "</span></span></a>";
		}

		$("#myownwork_sidebar").empty().append(rows);

		if (activeAccount) {
			$("#myownwork_sidebar a[data-account=" + activeAccount + "]").addClass("active").trigger("click");
		}

		NRS.pageLoaded(callback);
	}
	$("#myownwork_sidebar").on("click", "a", function(e) {
		computation_power=[];
		solution_rate=[];

		$("#myownwork_sidebar a.active").removeClass("active");
		if($(this).hasClass("selectable")){
			e.preventDefault();
			var arrayIndex = $(this).data("array-index");
			var workItem = _work[arrayIndex];

			$("#myownwork_sidebar a.active").removeClass("active");
			$(this).addClass("active");


			// Now fill the right side correctly
			$("#work_title_right").empty().append(workItem.title);

			// Percentages
			$("#bal_work").empty().append(workItem.percent_work);
			$("#bal_bounties").empty().append(workItem.percent_bounties);


			$("#bal_original").empty().append(NRS.formatAmount(workItem.balance_original));
			$("#bal_remained").empty().append(NRS.formatAmount(workItem.balance_remained));
			$("#bnt_connected").empty().append(workItem.bounties_connected);

			if(workItem.language=="LUA")
				$("#programming_language").empty().append("LUA (Version 1, Hardened)");

			$("#blockchain_bytes").empty().append(formatBytes(parseInt(workItem.script_size_bytes)));
			$("#fee").empty().append(NRS.formatAmount(workItem.fee));

			$("#num_in").empty().append(workItem.num_input);
			$("#num_out").empty().append(workItem.num_output);
			$("#percent_done").empty().append(workItem.percent_done);
			$("#progbar_work").attr("aria-valuenow",parseInt(workItem.percent_done));
			$("#progbar_work").css("width",workItem.percent_done + "%");


			// plot with loading indicator
			doPlot();
			// Now load real data
			NRS.sendRequest("getAccountWorkEfficiencyPlot", {
			"workId": workItem.workId
			}, function(response) {
				if (response.computation_power && response.solution_rate) {
					computation_power = response.computation_power;
					solution_rate = response.solution_rate;
					doPlot(); // refresh
				} 
			});


			$("#no_work_selected").hide();
			$("#work_details").show();
		}else{
			$("#no_work_selected").show();
			$("#work_details").hide();
		}
	});

	return NRS;
}(NRS || {}, jQuery));