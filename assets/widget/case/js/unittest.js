﻿/*Auto generate by UI designer */
(function($) {
    appcan.button("#nav-left", "btn-act", function() {
    });
    appcan.button("#nav-right", "btn-act", function() {
    });

    appcan.ready(function() {
        $.scrollbox($("body")).on("releaseToReload", function() {//After Release or call reload function,we reset the bounce
            $("#ScrollContent_dgMxRd").trigger("reload", this);
        }).on("onReloading", function(a) {//if onreloading status, drag will trigger this event

        }).on("dragToReload", function() {//drag over 30% of bounce height,will trigger this event

        }).on("draging", function(status) {//on draging, this event will be triggered.

        }).on("release", function() {//on draging, this event will be triggered.

        }).on("scrollbottom", function() {//on scroll bottom,this event will be triggered.you should get data from server
            $("#ScrollContent_dgMxRd").trigger("more", this);
        });
    })

    appcan.ready(function() {
        UNIT_TEST.start($("#Pane_s7tXbl"));
    })
    if (UNIT_TEST) {
        var uexFileMgrCase = {
            "create" : function() {
                var f = appcan.file.create("wgt://create.txt");
                if (f != null) {
                    f.close();
                }
                UNIT_TEST.assertNotEqual(f, 0);

            },
            "createSecure" : function() {
                var f = appcan.file.createSecure("wgt://createSecure.txt");
                if (f != null) {
                    f.close();
                }
                UNIT_TEST.assertNotEqual(f, 0);

            },
            "open" : function() {
                var f = appcan.file.open("wgt://demo.txt", 2);
                if (f != null) {
                    f.close();
                }
                UNIT_TEST.assertNotEqual(f, 0);
            },
            "openSecure" : function() {
                var f = appcan.file.openSecure("wgt://secure.txt", 2, "appcan.cn");
                if (f != null) {
                    f.close();
                }
                UNIT_TEST.assertNotEqual(f, 0);
            },
            "write" : function() {
                var f = appcan.file.open("wgt://demo.txt", 2);
                if (f != null) {
                    f.write("this is a case", 0, function(ret) {
                        alert(ret);
                        f.close();
                        UNIT_TEST.assertTrue(ret);
                    })
                } else
                    UNIT_TEST.assert(false);
            },
            "read" : function() {
                var f = appcan.file.open("wgt://demo.txt", 2);
                if (f != null) {
                    f.read(-1, 0, function(err, ret) {
                        f.close();
                        UNIT_TEST.log(err + "--" + ret);
                        if (!err)
                            UNIT_TEST.assertEqual(ret, "this is a case");
                        else
                            UNIT_TEST.assert(false);
                    })
                } else
                    UNIT_TEST.assert(false);
            },
            "deleteFile" : function() {
                var ret = appcan.file.remove("wgt://secure.txt");
                UNIT_TEST.assertTrue(ret);
            },
            "renameFile" : function() {
                appcan.file.rename("wgt://demo.txt", "wgt://open.txt", function(res) {
                    UNIT_TEST.assertTrue(!res);
                });
            },
            "createTime" : function() {
                var t = appcan.file.createTime("wgt://open.txt");
                UNIT_TEST.log("time is " + t);
                UNIT_TEST.assertString(t);
            },
            "getRealPath" : function() {
                var p = appcan.file.getRealPath("wgt://open.txt");
                UNIT_TEST.assertString(p);
            },
            "fileSize" : function() {
                var p = appcan.file.fileSize("wgt://open.txt", function(info) {
                    UNIT_TEST.log(JSON.stringify(info));
                    UNIT_TEST.assertTrue(info != null);
                });
            },
            "fileList" : function() {
                var info = appcan.file.fileList("wgt://");
                UNIT_TEST.log(JSON.stringify(info));
                UNIT_TEST.assertTrue(info != null);
            },
            "writeAll" : function() {
                appcan.file.writeAll("wgt://all.txt", "this is a case", function(error) {
                    UNIT_TEST.assertTrue(error);
                })
            },
            "readAll" : function() {
                appcan.file.readAll("wgt://all.txt", function(error, data) {
                    UNIT_TEST.log(error + "--" + data);
                    if (!error)
                        UNIT_TEST.assertEqual(data, "this is a case");
                    else
                        UNIT_TEST.assert(false);
                })
            },
            "writeAllSecure" : function() {
                appcan.file.writeAll("wgt://allSecure.txt", "this is a case", function(error) {
                    UNIT_TEST.assertTrue(error);
                }, "appcan.cn")
            },
            "readAllSecure" : function() {
                appcan.file.readAll("wgt://allSecure.txt", function(error, data) {
                    UNIT_TEST.log(error + "--" + data);
                    if (!error)
                        UNIT_TEST.assertEqual(data, "this is a case");
                    else
                        UNIT_TEST.assert(false);
                }, "appcan.cn")
            },
            "stat" : function() {
                UNIT_TEST.assertEqual(appcan.file.stat("wgt://all.txt"), 0)
            },
            "explorer" : function() {
                appcan.file.explorer("wgt://", function(error, path) {
                    UNIT_TEST.log(JSON.stringify(path));
                    if (!error) {
                        UNIT_TEST.assert(true);
                    } else
                        UNIT_TEST.assert(false);
                })
            },
            "multiExplorer" : function() {
                appcan.file.multiExplorer("wgt://", function(error, path) {
                    UNIT_TEST.log(JSON.stringify(path));
                    if (!error) {
                        UNIT_TEST.assert(true);
                    } else
                        UNIT_TEST.assert(false);
                })
            },
            "exists" : function() {
                UNIT_TEST.assertTrue(appcan.file.exists("wgt://all.txt"));
            }
        };

        var uexDatabaseMgrCase = {
            "open" : function() {
                var db = appcan.database.open("demo");
                if (db != null) {
                    db.close();
                }
                UNIT_TEST.assertNotEqual(db, 0);
            },
            "createTable" : function() {
                var db = appcan.database.open("demo");
                db && db.exec("CREATE TABLE testTable (_id  INTEGER PRIMARY KEY,name TEXT)", function(error, data) {
                    UNIT_TEST.log("CREATE TABLE:" + error + "--" + data);
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            },
            "insert" : function() {
                var db = appcan.database.open("demo");
                // db && db.exec("insert into testTable (_id,text) VALUES ("+(new Date()).valueOf()+",'this is a case')", function(error, data) {
                db && db.exec("INSERT INTO testTable (name) VALUES ('this is a case insert')", function(error, data) {

                    UNIT_TEST.log("INSERT INTO:" + error + "--" + data);
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            },
            "query" : function() {
                var db = appcan.database.open("demo");
                db && db.select("select * from testTable", function(error, data) {
                    UNIT_TEST.log("QUERY :" + error + "--" + JSON.stringify(data));
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            },
            "delete" : function() {
                var db = appcan.database.open("demo");
                db && db.exec("delete from testTable", function(error, data) {
                    UNIT_TEST.log("DELETE FROM: " + error + "--" + data);
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            },
            "transaction" : function() {
                var db = appcan.database.open("demo");
                var sql = ["DROP TABLE testTable", "CREATE TABLE testTable (_id  INTEGER PRIMARY KEY,name TEXT)", "insert into testTable (name) VALUES ('this is a case--1')", "UPDATE testTable SET name='这是更改' WHERE name = 'this is a case--1'"];
                db.transaction(JSON.stringify(sql), function(error, data) {
                    UNIT_TEST.log("TRANSACTION RESULT:" + error + "--" + data);
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            },
            "check" : function() {
                var db = appcan.database.open("demo");
                db && db.select("select * from testTable", function(error, data) {
                    UNIT_TEST.log("QUERY :" + error + "--" + JSON.stringify(data));
                    db.close();
                    UNIT_TEST.assert(!error && data[0].name == "这是更改");
                })
                !db && UNIT_TEST.assert(false);
            },
            "dropTable" : function() {
                var db = appcan.database.open("demo");
                db && db.exec("DROP TABLE testTable", function(error, data) {
                    UNIT_TEST.log("DROP TABLE:" + error + "--" + data);
                    db.close();
                    UNIT_TEST.assert(!error);
                })
                !db && UNIT_TEST.assert(false);
            }
        }
        var uexXMLHttpMgrCase = {
            "POST" : function() {
                var self = this;
                appcan.request.ajax({
                    url : "http://42.96.172.127:19898/app/login",
                    type : "POST",
                    data : $.param({"username":"admin","password":"admin"}),
                    dataType : "json",
                    contentType : "application/x-www-form-urlencoded",
                    success : function(data) {
                        UNIT_TEST.log("POST LOGIN: " + JSON.stringify(data));
                        UNIT_TEST.assertEqual(data.status, 0);
                    },
                    error : function(e, err) {
                        UNIT_TEST.log(e);
                        UNIT_TEST.assertNotEqual(data.status, 0);
                    }
                });
            }
        }
        UNIT_TEST.addCase("file", uexFileMgrCase);
    }
})($);
