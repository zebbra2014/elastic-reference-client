/**
 * @depends {nrs.js}
 * @depends {nrs.modals.js}
 */
var NRS = (function(NRS, $, undefined) {
	$("#transactions_table, #dashboard_transactions_table, #transfer_history_table, #exchange_history_table, #currencies_table, #transaction_info_table, #ms_exchanges_history_table, #ms_exchange_requests_table, #user_info_modal_currencies, #block_info_transactions_table, #user_info_modal_transactions_table, #ms_open_buy_orders_table, #ms_open_sell_orders_table, #hideable").on("click", "a[data-transaction]", function(e) {
		e.preventDefault();

		var transactionId = $(this).data("transaction");
		var infoModal = $('#transaction_info_modal');
		var isModalVisible = false;
		if (infoModal && infoModal.data('bs.modal')) {
			isModalVisible = infoModal.data('bs.modal').isShown;
		}
		NRS.showTransactionModal(transactionId, isModalVisible);
	});

	NRS.showTransactionModal = function(transaction, isModalVisible) {
		if (NRS.fetchingModalData) {
			return;
		}

		NRS.fetchingModalData = true;

		$("#transaction_info_output_top, #transaction_info_output_bottom, #transaction_info_bottom").html("").hide();
		$("#transaction_info_callout").hide();
		$("#transaction_info_table").hide();
		$("#transaction_info_table").find("tbody").empty();

		if (typeof transaction != "object") {
			NRS.sendRequest("getTransaction", {
				"transaction": transaction
			}, function(response, input) {
				response.transaction = input.transaction;
				NRS.processTransactionModalData(response, isModalVisible);
			});
		} else {
			NRS.processTransactionModalData(transaction, isModalVisible);
		}
	};

	NRS.processTransactionModalData = function(transaction, isModalVisible) {
		var async = false;

		var transactionDetails = $.extend({}, transaction);
		delete transactionDetails.attachment;
		if (transactionDetails.referencedTransaction == "0") {
			delete transactionDetails.referencedTransaction;
		}
		delete transactionDetails.transaction;

		if (!transactionDetails.confirmations) {
			transactionDetails.confirmations = "/";
		}
		if (!transactionDetails.block) {
			transactionDetails.block = "unconfirmed";
		}
		if (transactionDetails.height == 2147483647) {
			transactionDetails.height = "unknown";
		} else {
			transactionDetails.height_formatted_html = "<a href='#' data-block='" + String(transactionDetails.height).escapeHTML() + "'>" + String(transactionDetails.height).escapeHTML() + "</a>";
			delete transactionDetails.height;
		}
		$("#transaction_info_modal_transaction").html(String(transaction.transaction).escapeHTML());

		$("#transaction_info_tab_link").tab("show");

		$("#transaction_info_details_table").find("tbody").empty().append(NRS.createInfoTable(transactionDetails, true));
		$("#transaction_info_table").find("tbody").empty();

		var incorrect = false;

		if (transaction.senderRS == NRS.accountRS) {
			$("#transaction_info_actions").hide();
		} else {
			if (transaction.senderRS in NRS.contacts) {
				var accountButton = NRS.contacts[transaction.senderRS].name.escapeHTML();
				$("#transaction_info_modal_add_as_contact").hide();
			} else {
				var accountButton = transaction.senderRS;
				$("#transaction_info_modal_add_as_contact").show();
			}

			$("#transaction_info_actions").show();
			$("#transaction_info_actions_tab").find("button").data("account", accountButton);
		}

		if (transaction.type == 0) {
			switch (transaction.subtype) {
				case 0:
					var data = {
						"type": $.t("ordinary_payment"),
						"amount": transaction.amountNQT,
						"fee": transaction.feeNQT,
						"recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
						"sender": transaction.senderRS ? transaction.senderRS : transaction.sender
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				default:
					incorrect = true;
					break;
			}
		} else if (transaction.type == 1) {
			switch (transaction.subtype) {
				case 0:
					var message;

					var $output = $("#transaction_info_output_top");

					if (transaction.attachment) {
						if (transaction.attachment.message) {
							if (!transaction.attachment["version.Message"]) {
								try {
									message = converters.hexStringToString(transaction.attachment.message);
								} catch (err) {
									//legacy
									if (transaction.attachment.message.indexOf("feff") === 0) {
										message = NRS.convertFromHex16(transaction.attachment.message);
									} else {
										message = NRS.convertFromHex8(transaction.attachment.message);
									}
								}
							} else {
								message = String(transaction.attachment.message);
							}
							$output.html("<div style='color:#999999;padding-bottom:10px'><i class='fa fa-unlock'></i> " + $.t("public_message") + "</div><div style='padding-bottom:10px'>" + String(message).escapeHTML().nl2br() + "</div>");
						}

						if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
							$output.append("<div id='transaction_info_decryption_form'></div><div id='transaction_info_decryption_output' style='display:none;padding-bottom:10px;'></div>");

							if (NRS.account == transaction.recipient || NRS.account == transaction.sender) {
								var fieldsToDecrypt = {};

								if (transaction.attachment.encryptedMessage) {
									fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
								}
								if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
									fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
								}

								NRS.tryToDecrypt(transaction, fieldsToDecrypt, (transaction.recipient == NRS.account ? transaction.sender : transaction.recipient), {
									"noPadding": true,
									"formEl": "#transaction_info_decryption_form",
									"outputEl": "#transaction_info_decryption_output"
								});
							} else {
								$output.append("<div style='padding-bottom:10px'>" + $.t("encrypted_message_no_permission") + "</div>");
							}
						}
					} else {
						$output.append("<div style='padding-bottom:10px'>" + $.t("message_empty") + "</div>");
					}

					$output.append("<table><tr><td><strong>" + $.t("from") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "sender") + "</td></tr><tr><td><strong>" + $.t("to") + "</strong>:&nbsp;</td><td>" + NRS.getAccountLink(transaction, "recipient") + "</td></tr></table>").show();

					break;
				case 1:
					var data = {
						"type": $.t("alias_assignment"),
						"alias": transaction.attachment.alias,
						"data_formatted_html": transaction.attachment.uri.autoLink()
					};
					data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 2:
					var data = {
						"type": $.t("poll_creation"),
						"name": transaction.attachment.name,
						"description": transaction.attachment.description
					};
					data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 3:
					var data = {
						"type": $.t("vote_casting")
					};
					data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 4:
					var data = {
						"type": $.t("hub_announcement")
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 5:
					var data = {
						"type": $.t("account_info"),
						"name": transaction.attachment.name,
						"description": transaction.attachment.description
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 6:
					if (transaction.attachment.priceNQT == "0") {
						if (transaction.sender == transaction.recipient) {
							var type = $.t("alias_sale_cancellation");
						} else {
							var type = $.t("alias_transfer");
						}
					} else {
						var type = $.t("alias_sale");
					}

					var data = {
						"type": type,
						"alias_name": transaction.attachment.alias
					};

					if (type == $.t("alias_sale")) {
						data["price"] = transaction.attachment.priceNQT
					}

					if (type != $.t("alias_sale_cancellation")) {
						data["recipient"] = transaction.recipientRS ? transaction.recipientRS : transaction.recipient;
					}

					data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;

					if (type == $.t("alias_sale")) {
						var message = "";
						var messageStyle = "info";

						NRS.sendRequest("getAlias", {
							"aliasName": transaction.attachment.alias
						}, function(response) {
							NRS.fetchingModalData = false;

							if (!response.errorCode) {
								if (transaction.recipient != response.buyer || transaction.attachment.priceNQT != response.priceNQT) {
									message = $.t("alias_sale_info_outdated");
									messageStyle = "danger";
								} else if (transaction.recipient == NRS.account) {
									message = $.t("alias_sale_direct_offer", {
										"nxt": NRS.formatAmount(transaction.attachment.priceNQT)
									}) + " <a href='#' data-alias='" + String(transaction.attachment.alias).escapeHTML() + "' data-toggle='modal' data-target='#buy_alias_modal'>" + $.t("buy_it_q") + "</a>";
								} else if (typeof transaction.recipient == "undefined") {
									message = $.t("alias_sale_indirect_offer", {
										"nxt": NRS.formatAmount(transaction.attachment.priceNQT)
									}) + " <a href='#' data-alias='" + String(transaction.attachment.alias).escapeHTML() + "' data-toggle='modal' data-target='#buy_alias_modal'>" + $.t("buy_it_q") + "</a>";
								} else if (transaction.senderRS == NRS.accountRS) {
									if (transaction.attachment.priceNQT != "0") {
										message = $.t("your_alias_sale_offer") + " <a href='#' data-alias='" + String(transaction.attachment.alias).escapeHTML() + "' data-toggle='modal' data-target='#cancel_alias_sale_modal'>" + $.t("cancel_sale_q") + "</a>";
									}
								} else {
									message = $.t("error_alias_sale_different_account");
								}
							}
						}, false);

						if (message) {
							$("#transaction_info_bottom").html("<div class='callout callout-bottom callout-" + messageStyle + "'>" + message + "</div>").show();
						}
					}

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 7:
					var data = {
						"type": $.t("alias_buy"),
						"alias_name": transaction.attachment.alias,
						"price": transaction.amountNQT,
						"recipient": transaction.recipientRS ? transaction.recipientRS : transaction.recipient,
						"sender": transaction.senderRS ? transaction.senderRS : transaction.sender
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 8:
					var data = {
						"type": $.t("alias_deletion"),
						"alias_name": transaction.attachment.alias,
						"sender": transaction.senderRS ? transaction.senderRS : transaction.sender
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				default:
					incorrect = true;
					break;
			}
		} else if (transaction.type == 2) {
			switch (transaction.subtype) {
				case 0:
					var data = {
						"type": $.t("asset_issuance"),
						"name": transaction.attachment.name,
						"quantity": [transaction.attachment.quantityQNT, transaction.attachment.decimals],
						"decimals": transaction.attachment.decimals,
						"description": transaction.attachment.description
					};
					data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
					$("#transaction_info_callout").html("<a href='#' data-goto-asset='" + String(transaction.transaction).escapeHTML() + "'>Click here</a> to view this asset in the Asset Exchange.").show();

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 1:
					async = true;

					NRS.sendRequest("getAsset", {
						"asset": transaction.attachment.asset
					}, function(asset, input) {
						var data = {
							"type": $.t("asset_transfer"),
							"asset_name": asset.name,
							"quantity": [transaction.attachment.quantityQNT, asset.decimals]
						};

						data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
						data["recipient"] = transaction.recipientRS ? transaction.recipientRS : transaction.recipient;

						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
				case 2:
					async = true;

					NRS.sendRequest("getAsset", {
						"asset": transaction.attachment.asset
					}, function(asset, input) {
						var data = {
							"type": $.t("ask_order_placement"),
							"asset_name": asset.name,
							"quantity": [transaction.attachment.quantityQNT, asset.decimals],
							"price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " XEL",
							"total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " XEL"
						};

						data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
				case 3:
					async = true;

					NRS.sendRequest("getAsset", {
						"asset": transaction.attachment.asset
					}, function(asset, input) {
						var data = {
							"type": $.t("bid_order_placement"),
							"asset_name": asset.name,
							"quantity": [transaction.attachment.quantityQNT, asset.decimals],
							"price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " XEL",
							"total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " XEL"
						};
						data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
				case 4:
					async = true;

					NRS.sendRequest("getTransaction", {
						"transaction": transaction.attachment.order
					}, function(transaction, input) {
						if (transaction.attachment.asset) {
							NRS.sendRequest("getAsset", {
								"asset": transaction.attachment.asset
							}, function(asset) {
								var data = {
									"type": $.t("ask_order_cancellation"),
									"asset_name": asset.name,
									"quantity": [transaction.attachment.quantityQNT, asset.decimals],
									"price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " XEL",
									"total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " XEL"
								};
								data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
								$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
								$("#transaction_info_table").show();

								$("#transaction_info_modal").modal("show");
								NRS.fetchingModalData = false;
							});
						} else {
							NRS.fetchingModalData = false;
						}
					});

					break;
				case 5:
					async = true;

					NRS.sendRequest("getTransaction", {
						"transaction": transaction.attachment.order
					}, function(transaction) {
						if (transaction.attachment.asset) {
							NRS.sendRequest("getAsset", {
								"asset": transaction.attachment.asset
							}, function(asset) {
								var data = {
									"type": $.t("bid_order_cancellation"),
									"asset_name": asset.name,
									"quantity": [transaction.attachment.quantityQNT, asset.decimals],
									"price_formatted_html": NRS.formatOrderPricePerWholeQNT(transaction.attachment.priceNQT, asset.decimals) + " XEL",
									"total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.quantityQNT, transaction.attachment.priceNQT)) + " XEL"
								};
								data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
								$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
								$("#transaction_info_table").show();

								$("#transaction_info_modal").modal("show");
								NRS.fetchingModalData = false;
							});
						} else {
							NRS.fetchingModalData = false;
						}
					});

					break;
                case 6:
                    async = true;

                    NRS.sendRequest("getTransaction", {
                        "transaction": transaction.transaction
                    }, function(transaction) {
                        if (transaction.attachment.asset) {
                            NRS.sendRequest("getAsset", {
                                "asset": transaction.attachment.asset
                            }, function(asset) {
                                var data = {
                                    "type": $.t("dividend_payment"),
                                    "asset_name": asset.name,
                                    "amount_per_share": NRS.formatOrderPricePerWholeQNT(transaction.attachment.amountNQTPerQNT, asset.decimals) + " XEL",
                                    "height": transaction.attachment.height
                                };
  										  data["sender"] = transaction.senderRS ? transaction.senderRS : transaction.sender;
                                $("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
                                $("#transaction_info_table").show();

                                $("#transaction_info_modal").modal("show");
                                NRS.fetchingModalData = false;
                            });
                        } else {
                            NRS.fetchingModalData = false;
                        }
                    });
                    break;
				default:
					incorrect = true;
					break;
			}
		} else if (transaction.type == 3) {
			switch (transaction.subtype) {
				case 0:
					console.log(transaction);
					var language="";
					if(transaction.attachment.language=="1")
						language="LUA (Version 1, Hardened)";
					var data = {
						"type": $.t("work_created"),
						"work_amount": NRS.formatAmount(transaction.amountNQT) + " XEL",
						"fee": transaction.feeNQT,
						"work_title": transaction.attachment.title,
						"work_language": language
					};

					$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
					$("#transaction_info_table").show();

					break;
				case 1:
					async = true;

					NRS.sendRequest("getDGSGood", {
						"goods": transaction.attachment.goods
					}, function(goods) {
						var data = {
							"type": $.t("marketplace_removal"),
							"item_name": goods.name,
							"seller": NRS.getAccountFormatted(goods, "seller")
						};

						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
				case 2:
					async = true;

					NRS.sendRequest("getDGSGood", {
						"goods": transaction.attachment.goods
					}, function(goods) {
						var data = {
							"type": $.t("marketplace_item_price_change"),
							"item_name": goods.name,
							"new_price_formatted_html": NRS.formatAmount(transaction.attachment.priceNQT) + " XEL",
							"seller": NRS.getAccountFormatted(goods, "seller")
						};

						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
				case 3:
					async = true;

					NRS.sendRequest("getDGSGood", {
						"goods": transaction.attachment.goods
					}, function(goods) {
						var data = {
							"type": $.t("marketplace_item_quantity_change"),
							"item_name": goods.name,
							"delta_quantity": transaction.attachment.deltaQuantity,
							"seller": NRS.getAccountFormatted(goods, "seller")
						};

						$("#transaction_info_table").find("tbody").append(NRS.createInfoTable(data));
						$("#transaction_info_table").show();

						$("#transaction_info_modal").modal("show");
						NRS.fetchingModalData = false;
					});

					break;
			}	
		
		}
		if (!(transaction.type == 1 && transaction.subtype == 0)) {
			if (transaction.attachment) {
				if (transaction.attachment.message) {
					if (!transaction.attachment["version.Message"]) {
						try {
							message = converters.hexStringToString(transaction.attachment.message);
						} catch (err) {
							//legacy
							if (transaction.attachment.message.indexOf("feff") === 0) {
								message = NRS.convertFromHex16(transaction.attachment.message);
							} else {
								message = NRS.convertFromHex8(transaction.attachment.message);
							}
						}
					} else {
						message = String(transaction.attachment.message);
					}

					$("#transaction_info_output_bottom").append("<div style='padding-left:5px;'><label><i class='fa fa-unlock'></i> " + $.t("public_message") + "</label><div>" + String(message).escapeHTML().nl2br() + "</div></div>");
				}

				if (transaction.attachment.encryptedMessage || (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender)) {
					if (transaction.attachment.message) {
						$("#transaction_info_output_bottom").append("<div style='height:5px'></div>");
					}

					if (NRS.account == transaction.sender || NRS.account == transaction.recipient) {
						var fieldsToDecrypt = {};

						if (transaction.attachment.encryptedMessage) {
							fieldsToDecrypt.encryptedMessage = $.t("encrypted_message");
						}
						if (transaction.attachment.encryptToSelfMessage && NRS.account == transaction.sender) {
							fieldsToDecrypt.encryptToSelfMessage = $.t("note_to_self");
						}

						NRS.tryToDecrypt(transaction, fieldsToDecrypt, (transaction.recipient == NRS.account ? transaction.sender : transaction.recipient), {
							"formEl": "#transaction_info_output_bottom",
							"outputEl": "#transaction_info_output_bottom"
						});
					} else {
						$("#transaction_info_output_bottom").append("<div style='padding-left:5px;'><label><i class='fa fa-lock'></i> " + $.t("encrypted_message") + "</label><div>" + $.t("encrypted_message_no_permission") + "</div></div>");
					}
				}

				$("#transaction_info_output_bottom").show();
			}
		}
		

		if (incorrect) {
			$.growl($.t("error_unknown_transaction_type"), {
				"type": "danger"
			});

			NRS.fetchingModalData = false;
			return;
		}

		if (!async) {
			$("#transaction_info_modal").modal("show");
			NRS.fetchingModalData = false;
		}
	};

	NRS.formatCurrencyExchange = function(currency, transaction, type) {
		var rateUnitsStr = " [ XEL / " + currency.code + " ]";
		var data = {
			"type": type == "sell" ? $.t("sell_currency") : $.t("buy_currency"),
			"code": currency.code,
			"units": [transaction.attachment.units, currency.decimals],
			"rate": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.rateNQT, currency.decimals) + rateUnitsStr,
			"total_formatted_html": NRS.formatAmount(NRS.calculateOrderTotalNQT(transaction.attachment.units, transaction.attachment.rateNQT)) + " XEL"
		};
		var rows = "";
		NRS.sendRequest("getExchangesByExchangeRequest", {
			"transaction": transaction.transaction
		}, function(response) {
			var exchangedUnits = BigInteger.ZERO;
			var exchangedTotal = BigInteger.ZERO;
			if (response.exchanges && response.exchanges.length > 0) {
				rows = "<table class='table table-striped'><thead><tr>" +
				"<th>" + $.t("Date") + "</th>" +
				"<th>" + $.t("Units") + "</th>" +
				"<th>" + $.t("Rate") + "</th>" +
				"<th>" + $.t("Total") + "</th>" +
				"<tr></thead><tbody>";
				for (var i = 0; i < response.exchanges.length; i++) {
					var exchange = response.exchanges[i];
					exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
					exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
					rows += "<tr>" +
					"<td><a href='#' data-transaction='" + String(exchange.offer).escapeHTML() + "'>" + NRS.formatTimestamp(exchange.timestamp) + "</a>" +
					"<td>" + NRS.formatQuantity(exchange.units, exchange.decimals) + "</td>" +
					"<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals) + "</td>" +
					"<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
					"</td>" +
					"</tr>";
				}
				rows += "</tbody></table>";
				data["exchanges_formatted_html"] = rows;
			} else {
				data["exchanges"] = $.t("no_matching_exchange_offer");
			}
			data["units_exchanged"] = [exchangedUnits, currency.decimals];
			data["total_exchanged"] = NRS.formatAmount(exchangedTotal) + " [XEL]";
		}, null, false);
		return data;
	};

	NRS.formatCurrencyOffer = function(currency, transaction) {
		var rateUnitsStr = " [ XEL / " + currency.code + " ]";
		var buyOffer;
		var sellOffer;
		NRS.sendRequest("getOffer", {
			"offer": transaction.transaction
		}, function(response) {
			buyOffer = response.buyOffer;
			sellOffer = response.sellOffer;
		}, null, false);
		var data = {};
		if (buyOffer && sellOffer) {
			data = {
				"type": $.t("exchange_offer"),
				"code": currency.code,
				"buy_supply_formatted_html": NRS.formatQuantity(buyOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialBuySupply, currency.decimals) + ")",
				"buy_limit_formatted_html": NRS.formatQuantity(buyOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalBuyLimit, currency.decimals) + ")",
				"buy_rate_formatted_html": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.buyRateNQT, currency.decimals) + rateUnitsStr,
				"sell_supply_formatted_html": NRS.formatQuantity(sellOffer.supply, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.initialSellSupply, currency.decimals) + ")",
				"sell_limit_formatted_html": NRS.formatQuantity(sellOffer.limit, currency.decimals) + " (initial: " + NRS.formatQuantity(transaction.attachment.totalSellLimit, currency.decimals) + ")",
				"sell_rate_formatted_html": NRS.calculateOrderPricePerWholeQNT(transaction.attachment.sellRateNQT, currency.decimals) + rateUnitsStr,
				"expiration_height": transaction.attachment.expirationHeight
			};
		} else {
			data["offer"] = $.t("no_matching_exchange_offer");
		}
		var rows = "";
		NRS.sendRequest("getExchangesByOffer", {
			"offer": transaction.transaction
		}, function(response) {
			var exchangedUnits = BigInteger.ZERO;
			var exchangedTotal = BigInteger.ZERO;
			if (response.exchanges && response.exchanges.length > 0) {
				rows = "<table class='table table-striped'><thead><tr>" +
				"<th>" + $.t("Date") + "</th>" +
				"<th>" + $.t("Type") + "</th>" +
				"<th>" + $.t("Units") + "</th>" +
				"<th>" + $.t("Rate") + "</th>" +
				"<th>" + $.t("Total") + "</th>" +
				"<tr></thead><tbody>";
				for (var i = 0; i < response.exchanges.length; i++) {
					var exchange = response.exchanges[i];
					exchangedUnits = exchangedUnits.add(new BigInteger(exchange.units));
					exchangedTotal = exchangedTotal.add(new BigInteger(exchange.units).multiply(new BigInteger(exchange.rateNQT)));
					var exchangeType = exchange.seller == transaction.sender ? "Buy" : "Sell";
					if (exchange.seller == exchange.buyer) {
						exchangeType = "Same";
					}
					rows += "<tr>" +
					"<td><a href='#' data-transaction='" + String(exchange.transaction).escapeHTML() + "'>" + NRS.formatTimestamp(exchange.timestamp) + "</a>" +
					"<td>" + exchangeType + "</td>" +
					"<td>" + NRS.formatQuantity(exchange.units, exchange.decimals) + "</td>" +
					"<td>" + NRS.calculateOrderPricePerWholeQNT(exchange.rateNQT, exchange.decimals) + "</td>" +
					"<td>" + NRS.formatAmount(NRS.calculateOrderTotalNQT(exchange.units, exchange.rateNQT)) +
					"</td>" +
					"</tr>";
				}
				rows += "</tbody></table>";
				data["exchanges_formatted_html"] = rows;
			} else {
				data["exchanges"] = $.t("no_matching_exchange_request");
			}
			data["units_exchanged"] = [exchangedUnits, currency.decimals];
			data["total_exchanged"] = NRS.formatAmount(exchangedTotal) + " [XEL]";
		}, null, false);
		return data;
	};

	NRS.getUnknownCurrencyData = function(transaction) {
		var data = {
			"status": "Currency Deleted or not Issued",
			"type": transaction.type,
			"subType": transaction.subtype
		};
		return data;
	};

	$("#transaction_info_modal").on("hide.bs.modal", function(e) {
		NRS.removeDecryptionForm($(this));
		$("#transaction_info_output_bottom, #transaction_info_output_top, #transaction_info_bottom").html("").hide();
	});

	return NRS;
}(NRS || {}, jQuery));