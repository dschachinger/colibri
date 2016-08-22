import java.io.*;
import java.net.*;
try
    A = 1;
    temp = 25;
    lightstatus = 'off';
    tl = 0;
    tx = 0;
    tc = now;
    set_param(bdroot,'SimulationCommand','start');
    host = 'localhost';
    port = 7777;
    address = InetAddress.getByName(host);
    socket = Socket(address, port);
    os = socket.getOutputStream;
    %osw = OutputStreamWriter(os);
    bw = PrintWriter(os, true);
    %bw = BufferedWriter(osw);
 	%Get the return message from the server
    while A ~= 5
    is = socket.getInputStream;
    isr = InputStreamReader(is);
    br = BufferedReader(isr);
    message = br.readLine();
    disp(char(message));
    b = char(message);
    A = str2double(b);
    if (A == 2 || A == 3)
        na = A;
    end
    disp(['Message received from the server : ' char(message)]);
    switch A
        case 1
            tc = now;
            set_param(bdroot,'SimulationCommand','update');
            pause(3);
            %if na == 3
                %block = 'models/New_temperature1';
            %else
                block = 'models/To Workspace';
            %end
            rto = get_param(block, 'RuntimeObject');
            %disp(rto.InputPort(1).Data);
            t = rto.InputPort(1).Data;
            disp(t);
            bw.println(num2str(t));
            %disp(['Message sent to the server : ' char(t)]);
        case 2
            temp = t;
            tl = now;
            tx = 0;
            set_param(bdroot,'SimulationCommand','update');
            pause(3);
            block = 'models/To Workspace1';
            rto = get_param(block, 'RuntimeObject');
            t = rto.InputPort(1).Data;
	    lightstatus = 'on';
            disp(t);
            bw.println(t);
            %disp(['Message sent to the server : ' char(t)]);
        case 3
            temp = t;
            tx = now;
            tl = 0;
            set_param(bdroot,'SimulationCommand','update');
            pause(3);
	    lightstatus = 'off';
            block = 'models/To Workspace2';
            rto = get_param(block, 'RuntimeObject');
            disp(rto.InputPort(1).Data);
            t = rto.InputPort(1).Data;
            bw.println(t);
            %disp(['Message sent to the server : ' char(t)]);
        case 4
            bw.println(lightstatus);
	case 5
            socket.close();
    end
    end
    catch ME
     %   error(ME.message)
end
%Closing the socket
try
    socket.close();
    catch ME
    error(ME.identifier, 'Connection Error: %s', ME.message)
end
