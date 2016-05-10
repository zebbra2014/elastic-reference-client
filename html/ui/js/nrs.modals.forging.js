/**
 * @depends {nrs.js}
 * @depends {nrs.modals.js}
 */
var NRS = (function(NRS, $, undefined) {
	//todo: use a startForgingError function instaed!

	NRS.forms.startForgingComplete = function(response, data) {
		if ("deadline" in response) {
			$("#forging_indicator").removeClass("btn-danger");
			$("#forging_indicator").removeClass("btn-success");
			$("#forging_indicator").addClass("btn-success");
			$("#forging_indicator").html($.t("forging")).attr("data-i18n", "forging");
			$("#forging_indicator").show();
			NRS.isForging = true;
			$.growl($.t("success_start_forging"), {
				type: "success"
			});
		} else {
			NRS.isForging = false;
			$.growl($.t("error_start_forging"), {
				type: 'danger'
			});
		}
	}

	NRS.forms.stopForgingComplete = function(response, data) {
		if ($("#stop_forging_modal .show_logout").css("display") == "inline") {
			NRS.logout();
			return;
		}

		$("#forging_indicator").removeClass("btn-danger");
		$("#forging_indicator").removeClass("btn-success");
		$("#forging_indicator").addClass("btn-danger");
		$("#forging_indicator").html($.t("not_forging")).attr("data-i18n", "not_forging");
		$("#forging_indicator").show();

		NRS.isForging = false;

		if (response.foundAndStopped) {
			$.growl($.t("success_stop_forging"), {
				type: 'success'
			});
		} else {
			$.growl($.t("error_stop_forging"), {
				type: 'danger'
			});
		}
	}

	$("#forging_indicator").click(function(e) {
		e.preventDefault();

		if (NRS.downloadingBlockchain) {
			$.growl($.t("error_forging_blockchain_downloading"), {
				"type": "danger"
			});
		} else if (NRS.state.isScanning) {
			$.growl($.t("error_forging_blockchain_rescanning"), {
				"type": "danger"
			});
		/*} else if (NRS.accountInfo.effectiveBalanceNXT == 0) {
			if (NRS.lastBlockHeight >= NRS.accountInfo.currentLeasingHeightFrom && NRS.lastBlockHeight <= NRS.accountInfo.currentLeasingHeightTo) {
				$.growl($.t("error_forging_lease"), {
					"type": "danger"
				});
			} else {
				$.growl($.t("error_forging_effective_balance"), {
					"type": "danger"
				});
			}*/
		} else if ($("#forging_indicator").hasClass("btn-success")) {
			$("#stop_forging_modal").modal("show");
		} else {
			$("#start_forging_modal").modal("show");
		}
	});

	return NRS;
}(NRS || {}, jQuery));