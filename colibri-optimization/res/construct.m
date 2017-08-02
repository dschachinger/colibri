function [input, training] = construct(table,history_steps, future_steps)
% This function imports data from results file of the simulation.
% Import the entire spreadsheet as table excluding rows that are unimportable.

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

    % log message
    disp('MATLAB: construct start');
    
    % define length
    length = size(table,1);
    
    % table -> array
    table = table2array(table(1:length,2:end));    
    
    % add dummy column for time
    table = [ zeros(length,1) table ];

    % ----------------------------------------------------------------

    % import comfort
    comfort = NaN(19,2,length);

    comfort(2,1,1:length) = table(1:length,81)';
    comfort(3,1,1:length) = table(1:length,83)';
    comfort(4,1,1:length) = table(1:length,85)';
    comfort(5,1,1:length) = table(1:length,87)';
    comfort(6,1,1:length) = table(1:length,89)';
    comfort(7,1,1:length) = table(1:length,91)';
    comfort(8,1,1:length) = table(1:length,93)';
    comfort(9,1,1:length) = table(1:length,95)';
    comfort(10,1,1:length) = table(1:length,97)';
    comfort(11,1,1:length) = table(1:length,99)';
    comfort(12,1,1:length) = table(1:length,101)';
    comfort(13,1,1:length) = table(1:length,103)';
    comfort(14,1,1:length) = table(1:length,105)';
    comfort(15,1,1:length) = table(1:length,107)';
    comfort(16,1,1:length) = table(1:length,109)';
    comfort(2,2,1:length) = table(1:length,80)';
    comfort(3,2,1:length) = table(1:length,82)';
    comfort(4,2,1:length) = table(1:length,84)';
    comfort(5,2,1:length) = table(1:length,86)';
    comfort(6,2,1:length) = table(1:length,88)';
    comfort(7,2,1:length) = table(1:length,90)';
    comfort(8,2,1:length) = table(1:length,92)';
    comfort(9,2,1:length) = table(1:length,94)';
    comfort(10,2,1:length) = table(1:length,96)';
    comfort(11,2,1:length) = table(1:length,98)';
    comfort(12,2,1:length) = table(1:length,100)';
    comfort(13,2,1:length) = table(1:length,102)';
    comfort(14,2,1:length) = table(1:length,104)';
    comfort(15,2,1:length) = table(1:length,106)';
    comfort(16,2,1:length) = table(1:length,108)';

    % 0 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z001
    % 1 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z002
    % 2 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z003
    % 3 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z004
    % 4 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z005
    % 5 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z006
    % 6 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z007
    % 7 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z008
    % 8 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z009
    % 9 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z010
    % 10 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z011
    % 11 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z012
    % 12 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z013
    % 13 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z014
    % 14 - http://www.auto.tuwien.ac.at/example/Data_Humidity_Z015
    % 15 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z001
    % 16 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z002
    % 17 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z003
    % 18 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z004
    % 19 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z005
    % 20 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z006
    % 21 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z007
    % 22 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z008
    % 23 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z009
    % 24 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z010
    % 25 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z011
    % 26 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z012
    % 27 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z013
    % 28 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z014
    % 29 - http://www.auto.tuwien.ac.at/example/Data_Temp_Z015

    % ----------------------------------------------------------------

    % import influences
    influences = NaN(21,length);
    
    influences(1,1:length) = table(1:length,3)';
    influences(2,1:length) = table(1:length,2)';
    influences(3,1:length) = table(1:length,5)';
    influences(4,1:length) = table(1:length,4)';
    influences(5,1:length) = table(1:length,51)';
    influences(6,1:length) = table(1:length,53)';
    influences(7,1:length) = table(1:length,55)';
    influences(8,1:length) = table(1:length,57)';
    influences(9,1:length) = table(1:length,59)';
    influences(10,1:length) = table(1:length,61)';
    influences(11,1:length) = table(1:length,63)';
    influences(12,1:length) = table(1:length,65)';
    influences(13,1:length) = table(1:length,67)';
    influences(14,1:length) = table(1:length,69)';
    influences(15,1:length) = table(1:length,70)';
    influences(16,1:length) = table(1:length,72)';
    influences(17,1:length) = table(1:length,73)';
    influences(18,1:length) = table(1:length,75)';
    influences(19,1:length) = table(1:length,76)';
    influences(20,1:length) = table(1:length,77)';
    influences(21,1:length) = table(1:length,79)';

    % 0 - http://www.auto.tuwien.ac.at/example/Env_AirHumidityRatio
    % 1 - http://www.auto.tuwien.ac.at/example/Env_AirTemperature
    % 2 - http://www.auto.tuwien.ac.at/example/Env_DirectSolar
    % 3 - http://www.auto.tuwien.ac.at/example/Env_DiffuseSolar
    % 4 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z001
    % 5 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z002
    % 6 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z003
    % 7 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z005
    % 8 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z006
    % 9 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z007
    % 10 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z008
    % 11 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z010
    % 12 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z011
    % 13 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z011_Top
    % 14 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z012
    % 15 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z012_Top
    % 16 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z013
    % 17 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z013_Top
    % 18 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z014_Top
    % 19 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z015
    % 20 - http://www.auto.tuwien.ac.at/example/Env_Solar_Z015_Top

    % ----------------------------------------------------------------
    
    % import occupancy
    occupancy = NaN(19,length);
    
    occupancy(2,1:length) = table(1:length,6)';
    occupancy(3,1:length) = table(1:length,7)';
    occupancy(4,1:length) = table(1:length,8)';
    occupancy(5,1:length) = table(1:length,9)';
    occupancy(6,1:length) = table(1:length,10)';
    occupancy(7,1:length) = table(1:length,11)';
    occupancy(8,1:length) = table(1:length,12)';
    occupancy(9,1:length) = table(1:length,13)';
    occupancy(10,1:length) = table(1:length,14)';
    occupancy(11,1:length) = table(1:length,15)';
    occupancy(12,1:length) = table(1:length,16)';
    occupancy(13,1:length) = table(1:length,17)';
    occupancy(14,1:length) = table(1:length,18)';
    occupancy(15,1:length) = table(1:length,19)';
    occupancy(16,1:length) = table(1:length,20)';
    
    % 0 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z001
    % 1 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z002
    % 2 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z003
    % 3 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z004
    % 4 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z005
    % 5 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z006
    % 6 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z007
    % 7 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z008
    % 8 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z009
    % 9 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z010
    % 10 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z011
    % 11 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z012
    % 12 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z013
    % 13 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z014
    % 14 - http://www.auto.tuwien.ac.at/example/Data_Occ_Z015
    
    % ----------------------------------------------------------------
    
    % import production    
    production = NaN(2,length);
    
    production(2,1:length) = table(1:length,141)';    
    
    % 0 - http://www.auto.tuwien.ac.at/example/Energy_Grid
    % 1 - http://www.auto.tuwien.ac.at/example/Energy_PV
    
    % ----------------------------------------------------------------
    
    % import demand
    demand = NaN(1,length);
    
    demand(1,1:length) = (sum(table(1:length,21:50),2))' + (sum(table(1:length,110:139),2))';
    
    % 0 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Coal
    % 1 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#NaturalGas
    % 2 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Nuclear
    % 3 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Oil
    % 4 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Biofuel
    % 5 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Biomass
    % 6 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Geothermal
    % 7 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Hydropower
    % 8 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Solar
    % 9 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Wind
    % 10 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Electricity
    % 11 - https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Hydrogen
    
    % ----------------------------------------------------------------
    
    % import states
    states = NaN(4,length);
    
    states(1,1:length) = table(1:length,152)';
    states(2,1:length) = table(1:length,154)';
    states(3,1:length) = table(1:length,151)';
    states(4,1:length) = table(1:length,153)';
    
    upper = 60;
    lower = 10;
    step = 0.5;
    states(1,1:length) = (states(1,1:length) - lower) .* ((upper-lower)/step) ./ (upper-lower);
    
    upper = 100;
    lower = 0;
    step = 0.5;
    states(2,1:length) = (states(2,1:length) - lower) .* ((upper-lower)/step) ./ (upper-lower);
    
    upper = 30;
    lower = 10;
    step = 0.5;
    states(3,1:length) = (states(3,1:length) - lower) .* ((upper-lower)/step) ./ (upper-lower);
    
    upper = 100;
    lower = 0;
    step = 0.5;
    states(4,1:length) = (states(4,1:length) - lower) .* ((upper-lower)/step) ./ (upper-lower);
    
    % 0 - http://www.auto.tuwien.ac.at/example/Control_Cooling
    % 1 - http://www.auto.tuwien.ac.at/example/Control_Dehumidify
    % 2 - http://www.auto.tuwien.ac.at/example/Control_Heating
    % 3 - http://www.auto.tuwien.ac.at/example/Control_Humidify
    
    % ----------------------------------------------------------------
    
    % create training struct
    
    training.train_start = datetime('2015-01-01 00:15:00','InputFormat','yyyy-MM-dd hh:mm:ss');
    training.train_occupancy = occupancy(2:16,:);
    training.train_influences = influences;
    training.train_states = states;
    training.target_comfort = comfort;
    training.target_occupancy = occupancy;
    training.target_demand = demand;
    training.target_production = production;
    
    % ----------------------------------------------------------------
    
    % create input struct    
    first_future_time_step = (31*96) + 1;
    
    input.current_start = datetime('2015-02-01 00:15:00','InputFormat','yyyy-MM-dd hh:mm:ss');
    input.future_priority = ones(2,future_steps);
    input.future_prices = ones(2,future_steps);
    input.future_influences = influences(:,first_future_time_step : (first_future_time_step+future_steps-1));
    input.history_occupancy = occupancy(2:16,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.history_influences= influences(:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.history_states= states(:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.targets_comfort = comfort(:,:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.targets_occupancy = occupancy(:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.targets_demand = demand(:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.targets_production = production(:,(first_future_time_step-history_steps) : (first_future_time_step-1));
    input.initial_schedule = [];
    
    % ----------------------------------------------------------------
    
    % save structs
    save('training.mat','training');    
    save('input.mat','input');
    
    % ----------------------------------------------------------------
    
    % log message
    disp('MATLAB: construct end');
    
end