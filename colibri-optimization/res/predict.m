function [desired_comfort, expected_comfort, production, demand, occupancy, occupancy_binary] = predict(type, states, data, input, models)
% This is the prediction function that simulates the neural networks.

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

    % predict static values
    if strcmp(type,'static')
        
        % initialize non static variables
        expected_comfort = [];
        demand = [];
       
        % ------------------------------------------------------------

        % initilize binary occupancy values for objective function
        occupancy_binary = ones(data.zones,data.future_steps);
        occupancy = ones(size(data.zone_occupancy,2),data.future_steps);
        
        % iterate through occupancy models (per zone)
        for i=1:data.zones
            
            % check if occupancy is measured
            if any(data.zone_occupancy(i,:)) == 1
                
                % define parameters
                temp_net = models.occupancy(i).net;
                temp_input = data.input(logical(models.occupancy(i).features),:);
                temp_magnitude = models.occupancy(i).magnitude;
                temp_targets = input.targets_occupancy(i,:);
                
                % set future occupancy values in input matrix
                temp_output = simulate(data.future_steps, temp_net, temp_input, temp_magnitude,temp_targets);
                
                % set occupancy of current zone for all occupancy data services in this zone
                for j=1:size(data.zone_occupancy,2)
                    if data.zone_occupancy(i,j) == 1
                        occupancy(j,:) = temp_output;
                    end;
                end;
                
                % calculate binary occupancy
                for j=1:data.future_steps
                    if temp_output(1,j) == 0
                        occupancy_binary(i,j) = 0;
                    end;
                end;
            end;
        end;
        
        % ------------------------------------------------------------
                
        % read size of comfort estimation models
        [rows, columns] = size(models.desired);
        
        % initilize desired comfort values for objective function
        desired_comfort = ones(rows,columns,data.future_steps);
        
        % iterate through zones
        for i=1:rows
            
            % iterate through parameters
            for j=1:columns
                
                % check if parameter is monitored/controled in zone
                if data.monitor_indicator(i,j) == 1
                    
                    % define parameters
                    temp_net = models.desired(i,j).net;
                    temp_input = data.input(logical(models.desired(i,j).features),:);
                    temp_magnitude = models.desired(i,j).magnitude;
                    temp_targets = reshape(input.targets_comfort(i,j,:),[1,size(input.targets_comfort(i,j,:),3)]);
                    
                    % set desired comfort values
                    desired_comfort(i,j,:) = simulate(data.future_steps, temp_net, temp_input, temp_magnitude, temp_targets);
                    
                end;
            end;
        end;
        
        % ------------------------------------------------------------
        
        % read size of production estimation models
        [~, columns] = size(models.production);
        
        % initilize production values for constraints
        production = ones(columns,data.future_steps);
        
        % iterate through all supply units
        for i=1:columns
            
            % check if supply unit production can be predicted
            if data.supply_production(i) == 1
                
                % define parameters
                temp_net = models.production(i).net;
                temp_input = data.input(logical(models.production(i).features),:);
                temp_magnitude = models.production(i).magnitude;
                temp_targets = input.targets_production(i,:);
                
                % set comfort estimation values
                production(i,:) = simulate(data.future_steps, temp_net, temp_input, temp_magnitude, temp_targets);
            end;
        end;
    end;

    % ----------------------------------------------------------------

    % predict values per solution
    if ~strcmp(type,'static')
        
        % initialize static return values
        desired_comfort = [];
        occupancy = [];
        occupancy_binary = [];
        production = [];
        
        % update input matrix with states of current solution
        data.input(data.input_states:end,(data.history_steps + 1):end) = states;
        
        % ------------------------------------------------------------
        
        % get size of estimated comfort network matrix
        [rows,columns] = size(models.expected);
        
        % initilize comfort values
        expected_comfort = zeros(rows,columns,data.future_steps);
        
        % per zone
        for i2=1:rows
            
            % per environmental indoor parameter
            for j2=1:columns
                
                % check if comfort estimation is modeld
                if data.monitor_indicator(i2,j2) == 1
                    
                    % set comfort estimation values
                    expected_comfort(i2,j2,:) = simulate(data.future_steps, models.expected(i2,j2).net, data.input(logical(models.expected(i2,j2).features),:), models.expected(i2,j2).magnitude, reshape(input.targets_comfort(i2,j2,:),[1,size(input.targets_comfort(i2,j2,:),3)]));
                end;
            end;
        end;
        
        % ------------------------------------------------------------
        
        % get size of estimated demand networks row vector
        [~,columns] = size(models.demand);
        
        % initialize demand values
        demand = zeros(columns,data.future_steps);
        
        % per energy type
        for i2=1:columns
            
            % set demand estimation values
            demand(i2,:) = simulate(data.future_steps, models.demand(i2).net, data.input(logical(models.demand(i2).features),:), models.demand(i2).magnitude, input.targets_demand(i2,:));
        end;
    end;

    % ----------------------------------------------------------------

    % no log message

end
