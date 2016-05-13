/**
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $, undefined) {

	
	var newworkrow = "<a href='#' data-target='#new_work_modal' data-toggle='modal' class='list-group-item larger-sidebar-element grayadder'>";
		newworkrow += "<i class='fa fa-edit work-image-type fa-5x'></i><p class='composelabel'>Click here to compose a new job</p>"
		newworkrow += "</a>";

	var _work = {};

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
		return "<b>" + message.timeout_at_block + "</b> blocks"; 
	}
	function efficiency(efficiency){
		return "<b>1%</b> efficiency"; 
	}
	function statusspan(message){
		return "<span class='label label-success label12px'>Active</span>";
	}
	function balancespan(message){
		return "<span class='label label-white label12px'>Bal: " + message.balance_remained + "XEL</span>";
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
		$("#myownwork_sidebar a.active").removeClass("active");
		if($(this).hasClass("selectable")){
			e.preventDefault();
			var arrayIndex = $(this).data("array-index");
			var workItem = _work[arrayIndex];

			$("#myownwork_sidebar a.active").removeClass("active");
			$(this).addClass("active");



			// Now fill the right side correctly
			$("#work_title_right").empty().append(workItem.title);
			$("#bal_work").empty().append(workItem.balance_work);
			$("#bal_bounties").empty().append(workItem.balance_bounties);
			$("#bal_original").empty().append(workItem.balance_original);
			// END Now fill the right side correctly



			$("#no_work_selected").hide();
			$("#work_details").show();
		}else{
			$("#no_work_selected").show();
			$("#work_details").hide();
		}
	});

	return NRS;
}(NRS || {}, jQuery));