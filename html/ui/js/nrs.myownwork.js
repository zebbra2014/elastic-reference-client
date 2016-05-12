/**
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $, undefined) {
	var _messages = {};
	var _latestMessages = {};

	function workEntry(time_created, time_closed, was_cancel, title, account, language, code, bounty_hooks, num_input, num_output, balance_remained, balance_work, balance_bounties, percent_done, bounties_connected, refund, timeout_at_block){
		var workEntryItem = {
			"time_created": time_created,
			"time_closed": time_closed,
			"was_cancel": was_cancel,
		    "title": title,
		    "account": account,
		    "language": language,
		    "code": code,
		    "bounty_hooks": bounty_hooks,
		    "num_input": num_input,
		    "num_output": num_output,
		    "balance_remained": balance_remained,
		    "balance_work": balance_work,
		    "balance_bounties": balance_bounties,
		    "percent_done": percent_done,
		    "bounties_connected": bounties_connected,
		    "refund": refund,
		    "timeout_at_block": timeout_at_block
		};
		return workEntryItem;
	}

	NRS.pages.myownwork = function(callback) {
		_messages = [];

		$(".content.content-stretch:visible").width($(".page:visible").width());

		// USE TEST DATA FOR NOW
		var item1 = workEntry(1,0,0,"Prime Number Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", "", [], 0, 0, 1931, 1000,931,67,7,0,5525);
		var item2 = workEntry(3,9,2,"Hash Collision Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", "", [], 0, 0, 2009, 500,1509,25,2,0,6744);
		
		_messages.push(item1);
		_messages.push(item2);

		_messages.sort(function(a, b) {
				if (a.time_created > b.time_created) {
					return 1;
				} else if (a.time_created < b.time_created) {
					return -1;
				} else {
					return 0;
				}
			});

		displayWorkSidebar(callback);

		/*NRS.sendRequest("getAccountTransactions+", {
			"account": NRS.account,
			"firstIndex": 0,
			"lastIndex": 75,
			"type": 1,
			"subtype": 0
		}, function(response) {
			if (response.transactions && response.transactions.length) {
				for (var i = 0; i < response.transactions.length; i++) {
					var otherUser = (response.transactions[i].recipient == NRS.account ? response.transactions[i].sender : response.transactions[i].recipient);

					if (!(otherUser in _messages)) {
						_messages[otherUser] = [];
					}

					_messages[otherUser].push(response.transactions[i]);
				}
				
				displayMessageSidebar(callback);
			} else {
				$("#no_message_selected").hide();
				$("#no_messages_available").show();
				$("#messages_sidebar").empty();
				NRS.pageLoaded(callback);
			}
		});*/
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
		newworkrow = "<a href='#' data-target='#new_work_modal' data-toggle='modal' class='list-group-item larger-sidebar-element grayadder'>";
		newworkrow += "<i class='fa fa-edit work-image-type fa-5x'></i><p class='composelabel'>Click here to compose a new job</p>"
		newworkrow += "</a>";
		rows += newworkrow;
//"<div class='row'><div class='col-md-3'><i class='fa fa-tasks fa-fw'></i> " + status2Text(message) + "</div><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-3'><i class='fa fa-times-circle-o fa-fw'></i> " + timeOut(message) + "</div><div class='col-md-3'><i class='fa fa-rocket fa-fw'></i> " + efficiency(message) + "</div></div>"
		for (var i = 0; i < _messages.length; i++) {
			var message = _messages[i];
//<span class='label label-white label12px'><span class=''>" + statusText(message) + "</span>
//<span class='label label-primary label12px margin5px'>" + message.language + "</span><span class='label label-success label12px margin5px'>Active</span><span class='label label-warning label12px margin5px'>" + message.bounties_connected + " Solutions</span>

			rows += "<a href='#' class='list-group-item larger-sidebar-element'><p class='list-group-item-text agopullright'>" + balancespan(message) + " " + statusspan(message) + " <span class='label label-primary label12px'>" + message.language + "</span></p><span class='list-group-item-heading betterh4'>" + message.title + "</span><br><small>created 1 day ago (block #13318)</small><span class='middletext_list'>" + /* BEGIN GRID */ "<div class='row fourtwenty'><div class='col-md-3'><i class='fa fa-tasks fa-fw'></i> " + status2Text(message) + "</div><div class='col-md-3'><i class='fa fa-hourglass-1 fa-fw'></i> " + ETA(message) + "</div><div class='col-md-3'><i class='fa fa-times-circle-o fa-fw'></i> " + timeOut(message) + "</div><div class='col-md-3'><i class='fa fa-rocket fa-fw'></i> " + efficiency(message) + "</div></div>" /* END GRID */ + "</span></span></a>";
		}

		$("#myownwork_sidebar").empty().append(rows);

		if (activeAccount) {
			$("#myownwork_sidebar a[data-account=" + activeAccount + "]").addClass("active").trigger("click");
		}

		NRS.pageLoaded(callback);
	}

	return NRS;
}(NRS || {}, jQuery));