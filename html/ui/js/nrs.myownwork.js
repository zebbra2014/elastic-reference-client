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
		newworkrow += "<i class='fa fa-edit work-image-type fa-5x'></i><p class='composelabel'>Click here to compose a new job</p>";
		newworkrow += "</a>";

	var _work = [];
	var _workToIndex = {};
	var computation_power=[];
	var solution_rate=[];

    function updateIncoming(transactions){
    	if (transactions.length) {
				for (var i=0; i<transactions.length; i++) {
					var trans = transactions[i];
					if (trans.confirmed && trans.type == 3 && /* Subtype doesn't matter, we refresh in all cases */ trans.senderRS == NRS.accountRS) {
						NRS.sendRequest("getAccountWork", {
							"account": NRS.account,
							"onlyOneId": trans.transaction,
							"type": 1
						}, function(response) {
							if (!response.work_packages || response.work_packages.length==0) return;
							response.work_packages.forEach(function (s, i, o) {

								if (s) {
									// replace in sidebar
									console.log("Calling REPLACEINSIDEBAR");
									console.log(s);
									replaceInSidebar(s);
									updateWork(s.workId, s);

								}
							});
						});
					}
					if (!trans.confirmed && trans.type == 3 && trans.subtype == 0 && trans.senderRS == NRS.accountRS) {
				
						addUnconfirmedWork(trans);
					}
					if (!trans.confirmed && trans.type == 3 && trans.subtype == 1 && trans.senderRS == NRS.accountRS) {
						cancellingUnconfirmed(trans.attachment.id);
					}
				}
			}
    }
	NRS.incoming.myownwork = function(transactions) {
		if (NRS.hasTransactionUpdates(transactions)) {
			updateIncoming(transactions);
		}
	}

	NRS.pages.myownwork = function(callback) {
		_work = [];
		_workToIndex = {};
		$("#no_work_selected").show();
		$("#work_details").hide();
		$(".content.content-stretch:visible").width($(".page:visible").width());


		NRS.sendRequest("getAccountWork", {
			"account": NRS.account,
			"type": 1
		}, function(response) {
			if (response.work_packages && response.work_packages.length) {
				for (var i = 0; i < response.work_packages.length; i++) {
					updateWork(response.work_packages[i].workId, response.work_packages[i]);
				}
				displayWorkSidebar(callback);
			} else {
				$("#no_work_selected").show();
				$("#work_details").hide();
				$("#myownwork_sidebar").empty().append(newworkrow);;

			}

			// Also handle unconfirmed TX
			NRS.sendRequest("getUnconfirmedTransactions", {
				"account": NRS.account,
			}, function(response) {
				if (response.unconfirmedTransactions && response.unconfirmedTransactions.length) {
					updateIncoming(response.unconfirmedTransactions);
				}

				// finally do the callback
				NRS.pageLoaded(callback);

			});
		});

		
	}
	function statusText(message){
		return "<b>" + message.balance_remained + "+</b> XEL left, <b>7742</b> blocks left"; 
	}
	function status2Text(message){
		return "<b>" + message.percent_done + "%</b> done"; 
	}
	function ETA(message){
		
		if(message.cancellation_tx=="0" && message.last_payment_tx=="0")
			return "ETA <b><1.5h</b>"; 
		else if(message.cancellation_tx!="0" && message.last_payment_tx=="0")
			return "Job Closed";
		else if(message.cancellation_tx=="0" && message.last_payment_tx!="0")
			return "Job Closed";
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
	function writeIfTrue(msg,boolsche){
		if(boolsche)
			return msg;
		else
			return "";
	}
	function efficiency(message){
		return "<b>" + message.bounties_connected + "</b> bounties"; 
	}
	function statusspan(message){
		if(message.cancellation_tx=="0" && message.last_payment_tx=="0")
			return "<span id='activeLabel' class='label label-success label12px'>Active</span>";
		else if(message.cancellation_tx!="0" && message.last_payment_tx=="0")
			return "<span id='activeLabel' class='label label-danger label12px'>Cancelled</span>";
		else if(message.cancellation_tx=="0" && message.last_payment_tx!="0")
			return "<span id='activeLabel' class='label label-info label12px'>Completed</span>";
	}
	function statusspan_precancel(){
		return "<span id='activeLabel' class='label label-warning label12px'>Cancel Requested</span>";
		
	}

	function moneyReturned(message){
		return "<b>" + NRS.formatAmount(message.balance_remained) + " XEL</b> refunded";
	}
	function moneyPaid(message){
		return "<b>" + NRS.formatAmount(message.balance_original) + " XEL</b> paid out";
	}

	function balancespan(message){
		return writeIfTrue("<span class='label label-white label12px'>" + NRS.formatAmount(message.balance_remained) + " XEL</span>", message.cancellation_tx=="0" && message.last_payment_tx=="0");
	}

	function flopsFormatter(v, axis) {
        return v.toFixed(axis.tickDecimals) + "G";
    }
    function perHourFormatter(v, axis) {
        return v.toFixed(axis.tickDecimals) + "/h  ";
    }
	function time_ago(seconds){
		var time_formats = [
		    [60, 'seconds', 1], // 60
		    [120, '1 minute ago', '1 minute from now'], // 60*2
		    [3600, 'minutes', 60], // 60*60, 60
		    [7200, '1 hour ago', '1 hour from now'], // 60*60*2
		    [86400, 'hours', 3600], // 60*60*24, 60*60
		    [172800, 'yesterday', 'tomorrow'], // 60*60*24*2
		    [604800, 'days', 86400], // 60*60*24*7, 60*60*24
		    [1209600, 'last week', 'next week'], // 60*60*24*7*4*2
		    [2419200, 'weeks', 604800], // 60*60*24*7*4, 60*60*24*7
		    [4838400, 'last month', 'next month'], // 60*60*24*7*4*2
		    [29030400, 'months', 2419200], // 60*60*24*7*4*12, 60*60*24*7*4
		    [58060800, 'last year', 'next year'], // 60*60*24*7*4*12*2
		    [2903040000, 'years', 29030400], // 60*60*24*7*4*12*100, 60*60*24*7*4*12
		    [5806080000, 'last century', 'next century'], // 60*60*24*7*4*12*100*2
		    [58060800000, 'centuries', 2903040000] // 60*60*24*7*4*12*100*20, 60*60*24*7*4*12*100
		];
		var token = 'ago', list_choice = 1;

		if (seconds == 0) {
		    return 'just now'
		}
		if (seconds < 0) {
		    seconds = Math.abs(seconds);
		    token = 'from now';
		    list_choice = 2;
		}
		var i = 0, format;
		while (format = time_formats[i++])
		    if (seconds < format[0]) {
		        if (typeof format[2] == 'string')
		            return format[list_choice];
		        else
		            return Math.floor(seconds / format[2]) + ' ' + format[1] + ' ' + token;
		    }
		return "a long time ago";
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



	function bottom_status_row(message){
		if(message.cancellation_tx=="0" && message.last_payment_tx=="0"){
			return "<div class='row fourtwenty'><div class='col-md-3'><i class='fa fa-tasks fa-fw'></i> " + status2Text(message) + "</div><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-3'><i class='fa fa-times-circle-o fa-fw'></i> " + timeOut(message) + "</div><div class='col-md-3'><i class='fa fa-star-half-empty fa-fw'></i> " + efficiency(message) + "</div></div>";
		}
		else if(message.cancellation_tx!="0" && message.last_payment_tx=="0"){
			return "<div class='row fourtwenty'><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-6'><i class='fa fa-mail-reply fa-fw'></i> " + moneyReturned(message) + "</div><div class='col-md-3'><i class='fa fa-star-half-empty fa-fw'></i> " + efficiency(message) + "</div></div>";
		}
		else if(message.cancellation_tx=="0" && message.last_payment_tx!="0"){
			return "<div class='row fourtwenty'><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-6'><i class='fa fa-mail-forward fa-fw'></i> " + moneyPaid(message) + "</div><div class='col-md-3'><i class='fa fa-star-half-empty fa-fw'></i> " + efficiency(message) + "</div></div>";
		}
	}

	function blockToAgo(blockHeight){
		var span = NRS.lastBlockHeight - blockHeight;
		var minPerBlock = 1;
		var secondsPassed = minPerBlock*span*60;
		return time_ago(secondsPassed);
	}

	function updateWork(workId, workPackage){
		if(_workToIndex[workId]==null){
			_work.push(workPackage);
			for (var i = 0; i < _work.length; i++) {
					_workToIndex[_work[i].workId] = i;
			}
		}else{
			_work[_workToIndex[workId]]=workPackage;
		}
	}

	function replaceInSidebar(message){
		newElement = "<a href='#' data-workid='" + message.workId + "' class='list-group-item larger-sidebar-element selectable'><p class='list-group-item-text agopullright'>" + balancespan(message) + " " + statusspan(message) + " <span class='label label-primary label12px'>" + message.language + "</span></p><span class='list-group-item-heading betterh4'>" + message.title + "</span><br><small>created " + blockToAgo(message.block_height_created) + " (block #" + message.block_height_created + ")</small><span class='middletext_list'>" + /* BEGIN GRID */ bottom_status_row(message) /* END GRID */ + "</span></span></a>";
		if($("#myownwork_sidebar").children().filter('[data-workid="' + message.workId + '"]').length>0){
			$("#myownwork_sidebar").children().filter('[data-workid="' + message.workId + '"]').replaceWith(newElement);
		}else{
			console.log("ADDING");
			console.log(message);
			$(".grayadder").after(newElement);
		}
	}

	function addUnconfirmedWork(transactionObj){
		if($("#myownwork_sidebar").children().filter('[data-workid="' + transactionObj.transaction + '"]').length==0){
			newElement = "<a href='#' data-workid='" + transactionObj.transaction + "' class='list-group-item larger-sidebar-element selectable' ><p class='list-group-item-text agopullright'><span class='label label-danger label12px'>Unconfirmed Work</span></p><span class='list-group-item-heading betterh4'>" + transactionObj.attachment.title + "</span><br><div class='laterdiv'>Details will become visible after the first confirmation. Please hang tight!</div></a>";
			$(".grayadder").after(newElement);
		}
		
	}

	function cancellingUnconfirmed(workId){
		$("#myownwork_sidebar").children().filter('[data-workid="' + workId + '"]').find("#activeLabel").replaceWith(statusspan_precancel());
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
			rows += "<a href='#' data-workid='" + message.workId + "' class='list-group-item larger-sidebar-element selectable'><p class='list-group-item-text agopullright'>" + balancespan(message) + " " + statusspan(message) + " <span class='label label-primary label12px'>" + message.language + "</span></p><span class='list-group-item-heading betterh4'>" + message.title + "</span><br><small>created " + blockToAgo(message.block_height_created) + " (block #" + message.block_height_created + ")</small><span class='middletext_list'>" + /* BEGIN GRID */ bottom_status_row(message) /* END GRID */ + "</span></span></a>";
		}

		$("#myownwork_sidebar").empty().append(rows);

		if (activeAccount) {
			$("#myownwork_sidebar a[data-account=" + activeAccount + "]").addClass("active").trigger("click");
		}

		NRS.pageLoaded(callback);
	}

	$("#cancel_btn").click(function(e) {
		e.preventDefault();

		if (NRS.downloadingBlockchain) {
			$.growl($.t("error_forging_blockchain_downloading"), {
				"type": "danger"
			});
		} else if (NRS.state.isScanning) {
			$.growl($.t("error_forging_blockchain_rescanning"), {
				"type": "danger"
			});
		} else{
			$("#cancel_work_modal").modal("show");
		}
	});

	$("#myownwork_sidebar").on("click", "a", function(e) {
		var arrayIndex = $(this).data("workid");
		var realIndex = _workToIndex[arrayIndex];
		var workItem = null;
		if(realIndex!=null){
		  workItem = _work[realIndex];
		}

		if(workItem == null){
			$("#no_work_selected").hide();
			$("#no_work_confirmed").show();
			$("#work_details").hide();
			$("#myownwork_sidebar a.active").removeClass("active");
			if($(this).hasClass("selectable")){
				e.preventDefault();
				$(this).addClass("active");
			}
			return;
		}

		computation_power=[];
		solution_rate=[];

		// TODO, create labels
		$("#cancel_btn").hide();
		if(workItem.cancellation_tx=="0" && workItem.last_payment_tx=="0"){
			$("#work_indicator").removeClass("btn-success").removeClass("btn-warning").removeClass("btn-default").removeClass("btn-info").addClass("btn-success");
			$("#work_indicator_inner").empty().append("Active");
			$("#cancel_btn").show();
			if ($("#myownwork_sidebar").children().filter('[data-workid="' + workItem.workId + '"]').find(".label-warning").length>0){
				$("#work_indicator").removeClass("btn-warning").removeClass("btn-success").removeClass("btn-default").removeClass("btn-info").addClass("btn-warning");
				$("#work_indicator_inner").empty().append("Cancel Requested");
				$("#cancel_btn").hide();
			}
			
			$("#hideable").show();
			$("#balancelefttitle").empty().append("Balance Left");
			$("#detailedlisting").empty().append("[<a href=#'>breakdown</a>]");
		}
		else if(workItem.cancellation_tx!="0" && workItem.last_payment_tx=="0"){
			$("#work_indicator").removeClass("btn-warning").removeClass("btn-success").removeClass("btn-default").removeClass("btn-info").addClass("btn-default");
			$("#work_indicator_inner").empty().append("Cancelled");
			$("#hideable").show();
			$("#balancelefttitle").empty().append("Balance Refunded");
			$("#detailedlisting").empty().append("[<a href=# data-transaction='" + workItem.cancellation_tx + "'>payback TX</a>, <a href=#'>breakdown</a>]");
		}
		else if(workItem.cancellation_tx=="0" && workItem.last_payment_tx!="0"){
			$("#work_indicator").removeClass("btn-warning").removeClass("btn-success").removeClass("btn-default").removeClass("btn-info").addClass("btn-info");
			$("#work_indicator_inner").empty().append("Finished");
			$("#hideable").hide();
			$("#balancelefttitle").empty().append("...");
			$("#detailedlisting").empty().append("");
		}


		$("#myownwork_sidebar a.active").removeClass("active");
		if($(this).hasClass("selectable")){
			e.preventDefault();
			

			$("#myownwork_sidebar a.active").removeClass("active");
			$(this).addClass("active");

			$("#job_id").empty().append(workItem.workId);
			document.getElementById("workId").value = workItem.workId;

			// Now fill the right side correctly
			$("#work_title_right").empty().append(workItem.title);

			// Percentages
			$("#bal_work").empty().append(workItem.percent_work);
			$("#bal_bounties").empty().append(workItem.percent_bounties);


			$("#bal_original").empty().append(NRS.formatAmount(workItem.balance_original));
			$("#bal_remained").empty().append(NRS.formatAmount(workItem.balance_remained));
			$("#bnt_connected").empty().append(workItem.bounties_connected);

			$("#refund_calculator").empty().append(NRS.formatAmount(workItem.balance_remained));

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
			$("#no_work_confirmed").hide();
			$("#work_details").show();
		}else{
			$("#no_work_selected").show();
			$("#no_work_confirmed").hide();
			$("#work_details").hide();
		}
	});

	return NRS;
}(NRS || {}, jQuery));