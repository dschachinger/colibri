function [comfort, demand] = cosimulation(start,interval,setpoints,positions)
% This function runs the EnergyPlus simulation and returns the results.

% **************************************************************************************************
% * Copyright (c) 2016, Automation Systems Group, Institute of Computer Aided Automation, TU Wien
% * All rights reserved.
% * 
% * Redistribution and use in source and binary forms, with or without
% * modification, are permitted provided that the following conditions
% * are met:
% * 1. Redistributions of source code must retain the above copyright
% *    notice, this list of conditions and the following disclaimer.
% * 2. Redistributions in binary form must reproduce the above copyright
% *    notice, this list of conditions and the following disclaimer in the
% *    documentation and/or other materials provided with the distribution.
% * 3. Neither the name of the Institute nor the names of its contributors
% *    may be used to endorse or promote products derived from this software
% *    without specific prior written permission.
% * 
% * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
% * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
% * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
% * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
% * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
% * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
% * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
% * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
% * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
% * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
% * SUCH DAMAGE.
% *************************************************************************************************/

    % no log message
    
    % ----------------------------------------------------------------

    % read original schedule    
    base = fopen('base.csv');
    s = textscan(base, '%s%s%s%s%s%s%s%s%s', 'delimiter', ';');
    fclose(base);

    % ----------------------------------------------------------------

    % set temp start
    temp = start;
    
    % update setpoints
    for i =1:size(setpoints,1)

        % create date string
        temp = temp + minutes(interval);
        str = sprintf('%02d.%02d.%04d %02d:%02d',day(temp),month(temp),year(temp),hour(temp),minute(temp));

        % id of date string
        id = strncmp(s{1},str,size(str,2));

        % update schedule line
        for j=1:size(positions,2)
            s{positions(1,j)}{id} = sprintf('%0.2f',setpoints(i,j));
        end;
        
    end;

    % ----------------------------------------------------------------

    % create new schedule file
    out = fopen('temp.csv','w');

    % print header
    fprintf(out,'%s\n',s{1}{1,1});

    % print rows
    for i=2:size(s{1},1)
        line = sprintf('%s;%s;%s;%s;%s;%s;%s;%s;%s;\n',s{1}{i,1},s{2}{i,1},s{3}{i,1},s{4}{i,1},s{5}{i,1},s{6}{i,1},s{7}{i,1},s{8}{i,1},s{9}{i,1});
        fprintf(out,line);
    end
    
    % close file
    fclose(out);

    % ----------------------------------------------------------------

    % get start ticks
    tic
    
    % run simulation
    system('energyplus.bat sim AUT_Vienna.Schwechat.110360_IWEC');

    % get end ticks
    toc

    % ----------------------------------------------------------------
   
    % read original schedule (154 columns)   
    result = fopen('sim.csv');
    r = textscan(result, '%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s', 'delimiter', ',');
    fclose(result);
        
    % ----------------------------------------------------------------
   
    % set temp start
    temp = start;
    
    % initialize return values
    comfort = NaN(19,2,size(setpoints,1));
    demand = NaN(1,size(setpoints,1));
    
    % read results
    for i = 1:size(setpoints,1)

        % create date string
        temp = temp + minutes(interval);
        str = sprintf('%02d/%02d  %02d:%02d:00',month(temp),day(temp),hour(temp),minute(temp));
        
        if minute(temp) == 0 && hour(temp) == 0
            str = sprintf('%02d/%02d  24:00:00',month(temp),day(temp));
        end;
        
        % id of date string
        id = find(strncmp(r{1},str,size(str,2)));

        % temperature per zone
        comfort(2,2,i) = str2double(r{80}{id});
        comfort(3,2,i) = str2double(r{82}{id});
        comfort(4,2,i) = str2double(r{84}{id});
        comfort(5,2,i) = str2double(r{86}{id});
        comfort(6,2,i) = str2double(r{88}{id});
        comfort(7,2,i) = str2double(r{90}{id});
        comfort(8,2,i) = str2double(r{92}{id});
        comfort(9,2,i) = str2double(r{94}{id});
        comfort(10,2,i) = str2double(r{96}{id});
        comfort(11,2,i) = str2double(r{98}{id});
        comfort(12,2,i) = str2double(r{100}{id});
        comfort(13,2,i) = str2double(r{102}{id});
        comfort(14,2,i) = str2double(r{104}{id});
        comfort(15,2,i) = str2double(r{106}{id});
        comfort(16,2,i) = str2double(r{108}{id});
        
        % humidity per zone
        comfort(2,1,i) = str2double(r{81}{id});
        comfort(3,1,i) = str2double(r{83}{id});
        comfort(4,1,i) = str2double(r{85}{id});
        comfort(5,1,i) = str2double(r{87}{id});
        comfort(6,1,i) = str2double(r{89}{id});
        comfort(7,1,i) = str2double(r{91}{id});
        comfort(8,1,i) = str2double(r{93}{id});
        comfort(9,1,i) = str2double(r{95}{id});
        comfort(10,1,i) = str2double(r{97}{id});
        comfort(11,1,i) = str2double(r{99}{id});
        comfort(12,1,i) = str2double(r{101}{id});
        comfort(13,1,i) = str2double(r{103}{id});
        comfort(14,1,i) = str2double(r{105}{id});
        comfort(15,1,i) = str2double(r{107}{id});
        comfort(16,1,i) = str2double(r{109}{id});
        
        % sum of heating power [W] per interval
        demand(1,i) = str2double(r{110}{id}) + ...
            str2double(r{112}{id}) + ...
            str2double(r{114}{id}) + ...
            str2double(r{116}{id}) + ...
            str2double(r{118}{id}) + ...
            str2double(r{120}{id}) + ...
            str2double(r{122}{id}) + ...
            str2double(r{124}{id}) + ...
            str2double(r{126}{id}) + ...
            str2double(r{128}{id}) + ...
            str2double(r{130}{id}) + ...
            str2double(r{132}{id}) + ...
            str2double(r{134}{id}) + ...
            str2double(r{136}{id}) + ...
            str2double(r{138}{id});  
        
        % heating energy [kWh] per interval
        demand(1,i) = demand(1,i) * (15/60) / 1000;
        
    end;    

    % ----------------------------------------------------------------
    % no log message
    
end
