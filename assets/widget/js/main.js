var cText = 0;
var cJson = 1;
var cInt = 2;

function createFile(path) {
	if (!isnum('1')) {
		alert("Error");
		return;
	}
	alert(uexFileMgr.createFile('1', path));
	uexFileMgr.closeFile('1');
}

function createSecureFile(path, code) {
	alert(uexFileMgr.createSecure('100', path, code));
	//uexFileMgr.openSecure('100', path, '1', code);
	alert(uexFileMgr.writeFile('100', '0', 'success'));
	uexFileMgr.closeFile('100');
}

function readSecureFile(path, size, code) {
	uexFileMgr.openSecure('100', path, '1', code);
	uexFileMgr.readFile('100', size);
	uexFileMgr.closeFile('100');

}

function isnum(num) {
	if (num) {
		return true;
	} else {
		false;
	}
}

function createDir(path) {
	if (!isnum('1')) {
		alert("Error");
		return;
	}
	uexFileMgr.createDir('20', path);

	uexFileMgr.closeFile('20');
}

	function writeFile(path, data) {
		if (!isnum('1')) {
			alert("Error");
			return;
		}
		uexFileMgr.openFile('1', path, '1');
		uexFileMgr.writeFile('1', '1', data,function(result){
      alert(result);
    });
		uexFileMgr.closeFile('1');

	}

	function readFile(path, size) {
		if (!isnum('1')) {
			alert("Error");
			return;
		}
		uexFileMgr.openFile('1', path, '1');
		uexFileMgr.readFile('1', size,0,function(result){
      alert(result);
    });
		uexFileMgr.closeFile('1');

	}
function isFileExistByID(path) {
	if (!isnum('2')) {
		alert("Error");
		return;
	}
	uexFileMgr.createFile('2', path);
	uexFileMgr.isFileExistByID('2');
	uexFileMgr.closeFile('2');
}

function deleteFileById() {
	if (!isnum('3')) {
		alert("Error");
		return;
	}
	uexFileMgr.createFile('3', document.getElementById('dirPath').value);
	uexFileMgr.deleteFileByID('3');
	uexFileMgr.closeFile('3');
}

function getFileTypeByID() {
	if (!isnum('4')) {
		alert("Error");
		return;
	}
	uexFileMgr.createFile('4', document.getElementById('dirPath').value);
	uexFileMgr.getFileTypeByID('4');
	uexFileMgr.closeFile('4');
}

function seekFile() {
	if (!isnum('5')) {
		alert("Error");
		return;
	}
	uexFileMgr.openFile('5', document.getElementById('dirPath').value, '1');
	uexFileMgr.seekFile('5', '1');
	uexFileMgr.closeFile('5');
}

function seekBeginOfFile() {
	if (!isnum('6')) {
		alert("Error");
		return;
	}
	uexFileMgr.openFile('6', document.getElementById('dirPath').value, '1');
	uexFileMgr.seekBeginOfFile('6');
	uexFileMgr.closeFile('6');
}

function seekEndOfFile() {
	if (!isnum('7')) {
		alert("Error");
		return;
	}
	uexFileMgr.openFile('7', document.getElementById('dirPath').value, '1');
	uexFileMgr.seekEndOfFile('7');
	uexFileMgr.closeFile('7');
}

function explorer() {
	uexFileMgr.explorer(document.getElementById('file_path').value, function(data) {
		alert(data);
	});
}

function multiExplorer() {
	uexFileMgr.multiExplorer(document.getElementById('file_path').value, function(data) {
		alert(data);
	});
}

function getFileSize() {
	if (!isnum('8')) {
		alert("Error");
		return;
	}
	uexFileMgr.openFile('8', document.getElementById('dirPath').value, '1');
	uexFileMgr.getFileSize('8');
	uexFileMgr.closeFile('8');
}

function getFilePath() {
	if (!isnum('9')) {
		alert("Error");
		return;
	}
	uexFileMgr.openFile('9', document.getElementById('dirPath').value, '1');
	uexFileMgr.getFilePath('9');
	uexFileMgr.closeFile('9');
}

function getFileRealPath() {
	uexFileMgr.getFileRealPath(document.getElementById('filePath2').value);
}

function getFileListByPath() {
	uexFileMgr.getFileListByPath(document.getElementById('list_FilePath').value);
}

function copyFile() {
	var s = document.getElementById('srcFilePath').value;
	var o = document.getElementById('objPath').value;
	alert("s: " + s + "\no: " + o);

	uexFileMgr.copyFile('109', s, o, function(data) {
		alert(data);
	});
}
window.uexOnload = function() {
	uexWidgetOne.cbError = function(opCode, errorCode, errorInfo) {
		alert(errorInfo);
	};

	uexFileMgr.cbReadFile = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				if (opCode == 1 && dataType == 0) {
					document.getElementById('readData').value = data;
				}
				if (opCode == 100 && dataType == 0) {
					document.getElementById('readData2').value = data;
				}
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				alert("uex.cInt");
				break;
			default:
				alert("error");

		}

	}
	uexFileMgr.cbIsFileExistByPath = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("文件不存在");
				} else if (data == 1) {
					alert("文件存在");
				} else {
					alert(data);
				}
				break;
			default:
				alert("error");
		}
	}

	uexFileMgr.cbIsFileExistById = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("文件不存在");
				} else if (data == 1) {
					alert("文件存在");
				} else {
					alert(data);
				}
				break;
			default:
				alert("error");
		}

	}

	uexFileMgr.cbDeleteFileByPath = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("删除成功");
				} else {
					alert("删除失败");
				}
				break;
			default:
				alert("error");
		}
	}

	uexFileMgr.cbDeleteFileByID = function(opCode, dataType, data) {
		alert(data);
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("删除成功");
				} else {
					alert("删除失败");
				}
				break;
			default:
				alert("error");
		}
	}
	uexFileMgr.cbGetFileTypeByPath = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 1) {
					alert("是文件夹");
				} else if (data == 0) {
					alert("是文件");
				} else {
					alert(data);
				}
				break;
			default:
				alert("error");
		}

	}
	uexFileMgr.cbGetFileTypeById = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 1) {
					alert("是文件夹");
				} else if (data == 0) {
					alert("是文件");
				} else {
					alert(data);
				}
				break;
			default:
				alert("error");
		}

	}

	uexFileMgr.cbGetFileSize = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				alert("文件长度：" + data);
				break;
			default:
				alert("error");
		}
	}
	uexFileMgr.cbGetFilePath = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("文件路径：" + data);
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				alert("uex.cInt");
				break;
			default:
				alert("error");
		}

	}
	uexFileMgr.cbGetFileRealPath = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("文件的真实路径：" + data);
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				alert("uex.cInt");
				break;
			default:
				alert("error");

		}

	};


	cbMultiExplorer = function(data) {
		alert(data);
	};
	uexFileMgr.cbCreateSecure = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("创建加密文件成功");
				} else {
					alert("创建加密文件失败");
				}
				break;
			default:
				alert("error");
		}

	}
	uexFileMgr.cbCreateFile = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("创建文件成功");
				} else {
					alert("创建文件失败");
				}
				break;
			default:
				alert("error");
		}

	}
	uexFileMgr.cbCreateDir = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("创建文件夹成功");
				} else {
					alert("创建文件夹失败");
				}
				break;
			default:
				alert("error");
		}
	}
	uexFileMgr.cbOpenFile = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("打开文件成功");
				} else {
					alert("打开文件失败");
				}
				break;
			default:
				alert("error");

		}

	}
	uexFileMgr.cbOpenSecure = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert(opCode + "打开加密文件成功");
				} else {
					alert(opCode + "打开加密文件失败");
				}
				break;
			default:
				alert("error");

		}

	}
	uexFileMgr.cbWriteFile = function(opCode, dataType, data) {
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				break;
			case cInt:
				if (data == 0) {
					alert("写入文件成功");
				} else {
					alert("写入文件失败");
				}
				break;
			default:
				alert("error");

		}
	}
	uexFileMgr.cbGetFileListByPath = function(opCode, dataType, data) {
		console.log("data: " + data);
		switch (dataType) {
			case cText:
				alert("uex.cText");
				break;
			case cJson:
				alert("uex.cJson");
				document.getElementById("fileListData").value = data;
				break;
			case cInt:
				alert("uex.cInt");
				break;
			default:
				alert("error");
		}
	};
	uexFileMgr.cbSearch = function(info) {
		alert(info);
	};
	uexFileMgr.cbGetFileSizeByPath = function(info) {
		alert(info);
	};
	uexFileMgr.cbCopyFile = function(opCode, dataType, data) {
		alert("opCode:" + opCode + ",dataType:" + dataType + ",data" + data);

	};
};

function getFileSizeByPath() {
	var path = document.getElementById("fileSize_id").value;
	var params = {
		id: 1,
		path: path,
		unit: "kB"
	};
	var data = JSON.stringify(params);
	uexFileMgr.getFileSizeByPath(data, function(data) {
		alert(JSON.stringify(data));
	});
}

function searchFile() {
	var data1 = {
		path: "/sdcard/wgtRes",
		option: 3
	};
	var data2 = {
		path: "res://",
		option: 3,
		keywords: ['test']
	};
	var data3 = {
		path: "file:///sdcard/wgtRes/",
		option: 5,
		keywords: ['1']
	};
	var data4 = {
		path: "res://",
		option: 6,
		keywords: ['1'],
		suffixes: ['xml']
	};
	var data5 = {
		path: "res://",
		option: 7,
		keywords: ['1']
	};
	var data6 = {
		path: "/sdcard/wgtRes",
		option: 5,
		keywords: ['1']
	};
	var data7 = {
		path: "wgts://",
		option: 5,
		keywords: ['1']
	};
	var data8 = {
		path: "wgts://",
		option: 7,
		keywords: ['1']
	};

	uexFileMgr.search(JSON.stringify(data1), function(data) {
		alert(JSON.stringify(data));
	});
	//uexFileMgr.search(JSON.stringify(data2));
	//uexFileMgr.search(JSON.stringify(data3));
	//uexFileMgr.search(JSON.stringify(data4));
	//uexFileMgr.search(JSON.stringify(data5));
	//uexFileMgr.search(JSON.stringify(data7));
	//uexFileMgr.search(JSON.stringify(data8));

}
