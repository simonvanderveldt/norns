// the Crone, a singleton class
// it receives OSC from *matron* and manages the current CroneEngine
Crone {
	classvar engine; // current CroneEngine instance
	classvar oscfunc;
	classvar remote_addr;
	classvar tx_port = 8888;
	classvar remote_addr;

	*initClass {
		StartUp.add { // defer until after sclang init

			postln("\n-------------------------------------------------");
			postln(" Crone startup");
			postln("");
			postln(" \OSC rx port: " ++ NetAddr.langPort);
			postln(" \OSC tx port: " ++ tx_port);
			postln("--------------------------------------------------\n");

			Server.default.boot;

			// FIXME: hardcoded remote address for now
			remote_addr =NetAddr("127.0.0.1", tx_port);

			// FIXME: osc patterns should be customized by current engine
			// this is dependent on adding support to the C side
			oscfunc = (
				
				'/report/engines':OSCFunc.new({
					arg msg, time, addr, recvPort;
					[msg, time, addr, recvPort].postln;
					this.reportEngines;
				}, '/report/engines'),
				
				'/report/commands':OSCFunc.new({
					arg msg, time, addr, recvPort;
					[msg, time, addr, recvPort].postln;
					this.reportCommands;
				}, '/report/commands'),
				
				'/engine/kill':OSCFunc.new({
					if(engine.notNil, { engine.kill; });
				}, '/engine/kill'),


				'/engine/load/name':OSCFunc.new({
					arg msg, time, addr, recvPort;
					[msg,time,addr,recvPort].postln;
					this.setEngine("Crone_" ++ msg[1]);
				}, '/engine/load/name')

				/*
				//------------------------------------------
				'/param/request/report':OSCFunc.new({
					arg msg, time, addr, recvPort;
					[msg, time, addr, recvPort].postln;
					this.reportParams;
				}, '/param/request/report'),

				'/param/set/name':OSCFunc.new({
					arg msg, time, addr, recvPort;
					engine.setParamByName(msg[1], msg[2]);
				}, '/param/set/name'),

				'/param/set/idx':OSCFunc.new({
					arg msg, time, addr, recvPort;
					engine.setParamByIndex(msg[1], msg[2]);
				}, '/param/set/idx'),


				//------------------------------------------
				'/buffer/request/report':OSCFunc.new({
					arg msg, time, addr, recvPort;
					[msg, time, addr, recvPort].postln;
					this.reportBuffers;
				}, '/buffer/request/report'),

				'/buffer/load/name':OSCFunc.new({
					arg msg, time, addr, recvPort;
					engine.loadBufferByName(msg[1], msg[2]);
				}, '/buffer/load/name'),

				'/buffer/save/name':OSCFunc.new({
					arg msg, time, addr, recvPort;
					engine.saveBufferByName(msg[1], msg[2]);
				}, '/buffer/save/name')
				*/
			);
		}
	}

	*setEngine { arg name;
		var class;
		class = CroneEngine.subclasses.select({ arg n; n.asString == name.asString })[0];
		postln("setEngine class: " ++ class);
		if(class.notNil, {
			if(engine.notNil, {
				engine.kill;
			});
			engine = class.new(Server.default);
			postln("new engine: " ++ engine);
		});

	}

	*reportEngines{
		var names = CroneEngine.subclasses.collect({ arg n;
			n.asString.split($_)[1]
		});
		postln("engines: " ++ names);
		postln("engines report start");
		remote_addr.sendMsg("/report/engines/start", names.size);
		names.do({ arg name, i;
			remote_addr.sendMsg("/report/engines/entry", i, name);

		});
		remote_addr.sendMsg("/report/engines/end");
		postln("engines report end");
	}

	*reportCommands {
		var cmds = engine.commands;
		postln("commands: " ++ cmds);
		postln("commands report start");
		remote_addr.sendMsg("/report/commands/start", cmds.size);
		cmds.do({ arg cmd, i;
			postln("command entry: " ++ [i, cmd.name, cmd.format]);
			remote_addr.sendMsg("/report/commands/entry", i, cmd.name, cmd.format);
		});
		remote_addr.sendMsg("/report/commands/end");
		postln("commands report end");
	}

	/*
	*reportBuffers{
		var names = engine.buffers;
		postln("buffers report start");
		remote_addr.sendMsg("/buffer/report/start", names.size);
		names.do({ arg name, i; remote_addr.sendMsg("/buffer/report/name", i, name); });
		remote_addr.sendMsg("/buffer/report/end");
		postln("buffers report end");
	}

	*reportParams {
		var names = engine.paramNames.keys;
		postln("params report start");
		remote_addr.sendMsg("/param/report/start", names.size);
		names.do({
			arg name, i; remote_addr.sendMsg("/param/report/name", i, name);
			postln("sent report for param " ++ i ++ " : " ++ name);
		});
		remote_addr.sendMsg("/param/report/end");
		postln("params report end");
	}
	*/
}