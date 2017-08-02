function [result] = model(path)
% This function initializes the forecast models.

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
    disp('MATLAB: model start');
    
    % ----------------------------------------------------------------
    
    % load variables
    load(strcat(path,'/training.mat'),'training');    
    load(strcat(path,'/data.mat'),'data');

    % ----------------------------------------------------------------
    
    % create input matrix for training
    train = combine( ...
        training.train_start, ...
        data.step_interval, ...
		(data.input_occupancy - data.input_time), ...
        size(training.train_occupancy,2), ...
        training.train_occupancy, ...
        training.train_influences, ...
        training.train_states, ...
        0, ...
        [], ...
        [], ...
        []);

    % initialize model struct
    models.occupancy = struct([]);
    models.desired = struct([]);
    models.production = struct([]);
    models.demand = struct([]);
    models.expected = struct([]);

    % ----------------------------------------------------------------
    
    % iterate through all zones to create OCCUPANCY MODELS
    for i=1:data.zones

        % create occupancy model if zone has occupancy measurement
        if any(data.zone_occupancy(i,:)) == 1

            % log message
            fprintf('MATLAB: model (start occupancy, zone=%d)\n',i);
            
            % build binary feature vector (time, influences)
            binary = zeros(1,data.input_end);
            binary(1:(data.input_occupancy - data.input_time)) = 1;
            binary(data.input_influences:(data.input_states - 1)) = any(data.occupancy_outdoor(logical(data.zone_occupancy(i,:)),:));

            % create model and feature vector
            node = design(2, binary, data, train, training.target_occupancy(i,:));

            % set model and feature vector
            models.occupancy(1,i).net = node.net;            
            models.occupancy(1,i).features = node.features;
            models.occupancy(1,i).magnitude = node.magnitude;
            
            % log message
            fprintf('MATLAB: model (end occupancy, zone=%d)\n', i);
        end;
    end;
    
    % save models struct
    save(strcat(path,'/models.mat'),'models');
    
    % ----------------------------------------------------------------
    
    % iterate through all zones to create COMFORT MODELS
    for i=1:data.zones
        
        % iterate through all environmental indoor parameters
        for j=1:data.parameters

            % create comfort models if monitor/control indicator is true
            if data.monitor_indicator(i,j) == 1

                % log message
                fprintf('MATLAB: model (start desired comfort, zone=%d,parameter=%d)\n',i,j);
            
                % build binary feature vector (time, occupancy, influences)
                binary = zeros(1,data.input_end);
                binary(1:(data.input_occupancy - 1)) = 1;
                binary(data.input_occupancy:(data.input_influences - 1)) = data.zone_occupancy(i,:);
                
                monitors = data.zone_parameter_monitor(i,j,:);
                monitors = reshape(monitors,[1,size(monitors,3)]);                
                binary(data.input_influences:(data.input_states - 1)) = data.indoor_outdoor(logical(monitors),:);

                % create MODEL FOR DESIRED COMFORT and its feature vector
                node = design(3, binary, data, train, reshape(training.target_comfort(i,j,:),[1, size(training.target_comfort(i,j,:),3)]));
            
                % set model and feature vector           
                models.desired(i,j).net = node.net;
                models.desired(i,j).features = node.features;
                models.desired(i,j).magnitude = node.magnitude;

                % log message
                fprintf('MATLAB: model (end desired comfort, zone=%d,parameter=%d)\n',i,j);
                
                % log message
                fprintf('MATLAB: model (start estimated comfort, zone=%d, parameter=%d)\n',i,j);
                
                % update binary feature vector (states)                
                controls = data.zone_parameter_control(i,j,:);
                controls = reshape(controls,[1,size(controls,3)]);
                binary(data.input_states:data.input_end) = controls;

                % create MODEL FOR COMFORT ESTIMATION and its feature vector
                node = design(3, binary, data, train, reshape(training.target_comfort(i,j,:),[1, size(training.target_comfort(i,j,:),3)]));

                % set model and feature vector
                models.expected(i,j).net = node.net;
                models.expected(i,j).features = node.features;
                models.expected(i,j).magnitude = node.magnitude;
                
                % log message
                fprintf('MATLAB: model (end estimated comfort, zone=%d,parameter=%d)\n',i,j);
            end;
        end;
    end;
    
    % save models struct again
    save(strcat(path,'/models.mat'),'models');
        
    % ----------------------------------------------------------------
    
    % create PRODUCTION model per supply unit
    for i=1:data.suppliers 

        % create production model if available
        if data.supply_production(i) == 1

            % log message
            fprintf('MATLAB: model (start production, supplier=%d)\n', i);
            
            % build binary feature vector (time, influences)
            binary = zeros(1,data.input_end);
            binary(1:(data.input_occupancy - data.input_time)) = 1;
            binary(data.input_influences:(data.input_states - 1)) = data.supply_outdoor(i,:);

            % create model and feature vector
            node = design(1, binary, data, train, training.target_production(i,:));

            % set model and feature vector            
            models.production(1,i).net = node.net;
            models.production(1,i).features = node.features;
            models.production(1,i).magnitude = node.magnitude;
            
            % log message
            fprintf('MATLAB: model (end production, supplier=%d)\n',i);
        end;
    end;
    
    % save models struct again
    save(strcat(path,'/models.mat'),'models');
    
    % ----------------------------------------------------------------
    
    % create DEMAND model per energy type
    for i=1:data.grids

        % log message
        fprintf('MATLAB: model (start demand, energy type=%d)\n', i);
            
        % build binary feature vector (time, influences)
        binary = zeros(1,data.input_end);
        binary(1:(data.input_occupancy - data.input_time)) = 1;
        binary(data.input_influences:(data.input_states - 1)) = data.grid_outdoor(i,:);
        binary(data.input_states:data.input_end) = data.grid_consumer(i,:);

        % get created model and feature vector
        node = design(3, binary, data, train, training.target_demand(i,:));

        % set model and feature vector
        models.demand(1,i).net = node.net;
        models.demand(1,i).features = node.features;
        models.demand(1,i).magnitude = node.magnitude;
        
        % log message
        fprintf('MATLAB: model (end demand, energy type=%d)\n', i);
    end;
    
    % save models struct again
    save(strcat(path,'/models.mat'),'models');

    % ----------------------------------------------------------------
    
    % set dummy result
    result = 1;
    
    % log message
    disp('MATLAB: model end');
    
end