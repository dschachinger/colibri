function [ mutation ] = move(x, m, tolCon, ConstrFcn)
% This is the move operation function for optimization.

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

    % define globals
    global data;
    global constraints;
    global keys;
    global values;
        
    % ----------------------------------------------------------------

    % define block size
    width = size(x,2) / data.future_steps;
    
    % initialize stopping counter
    valCount = 0;

    % validity indicator
    valid = false;
    
    % ----------------------------------------------------------------

    % get index of solution
    index = 0;
    if ~isempty(keys)
        [~, index] = ismember(x,keys,'rows');
    end;

    % are the consequences already available
    if index > 0
        fprintf('MATLAB: mutate (solution consequences are found)\n');

        % load predicted values
        v = values(index).v;
        d = values(index).d;

    else

        % throw exception
        msgID = 'mutate:BadIndex';
        msg = 'Unable to find solution consequences.';
        baseException = MException(msgID,msg);
        throw(baseException);
    end;

    % split solution
    [q, l, s] = split(x, data);
    
    % ----------------------------------------------------------------

    % re-run crossover until solution is feasible
    while ~valid

        % exit loop if solution does not get valid
        if valCount >= data.validity_count
            break;
        end;

        % temporary solution
        x_temp = x;

        switch m

            % mutation needs to ensure (implicit) integer constraints
            % maybe execute local search on new mutation children (also for crossover)

            case 1 % mutation of comfort

                next = false;
                omit = zeros(data.zones,data.parameters);

                while ~next

                    % find max deviation => select random in order to avoid endless loop
                    % maxDeviation = 0;
                    maxStep = randi(data.future_steps);
                    maxParameter = randi(data.parameters);
                    maxZone = randi(data.zones);

                    % for t=1:data.future_steps
                    %    for p=1:data.parameters
                            for z=1:data.zones
                                if data.monitor_indicator(z,maxParameter) == 1 && data.occupancy(z, maxStep) == 1
                                    % deviation = (v(z,p,t) - data.desired(z,p,t))^2;
                    
                                    % if deviation > maxDeviation && omit(z,p) == 0
                                    if omit(z,maxParameter) == 0
                                        % maxDeviation = deviation;
                                        % maxStep = t;
                                        % maxParameter = p;
                                        maxZone = z;
                                    end;
                                end;
                            end;
                    %    end;
                    % end;

                    % controls for this zone and this parameter
                    controls = data.zone_parameter_control(maxZone,maxParameter,:);

                    % positive: too high, negative: too low
                    direction = v(maxZone,maxParameter,maxStep) - data.desired(maxZone,maxParameter,maxStep);

                    % available controls
                    controls = reshape(controls,[1,size(controls,3)]);

                    % control variations for this parameter
                    if direction > 0 % downwards
                        higher = sum(reshape(data.control_parameter_variation(:,2,:),[4,2])==2,2);
                        lower = sum(reshape(data.control_parameter_variation(:,2,:),[4,2])==4,2);
                    else % upwards
                        higher = sum(reshape(data.control_parameter_variation(:,2,:),[4,2])==1,2);
                        lower = sum(reshape(data.control_parameter_variation(:,2,:),[4,2])==3,2);
                    end;

                    % combine both control sets
                    higher = controls & higher';
                    lower = controls & lower';

                    % number of available control variations
                    count = sum(higher) + sum(lower);

                    % variation found
                    if count > 0
                        next = true;
                    % else
                    %    omit(maxZone,maxParameter) = 1;
                    end;
                end;

                % mutate state
                success = false;
                while ~success

                    select = randi(count);

                    num = 1;
                    for ci=1:size(controls,2)
                        if higher(1,ci) == 1
                            if num == select

                                success = true;
                                currentState = l(ci,maxStep);
                                ub = constraints.upper_bounds(1,(maxStep-1)*width + data.suppliers + ci);

                                if currentState + 1 <= ub
                                    x_temp((maxStep-1)*width + data.suppliers + ci) = round(currentState + 1);
                                    break;
                                end;
                            end;
                            num = num + 1;
                        end;
                    end;

                    if success == false
                        for ci=1:size(controls,2)
                            if lower(1,ci) == 1
                                if num == select

                                    success = true;
                                    currentState = l(ci,maxStep);
                                    lb = constraints.lower_bounds(1,(maxStep-1)*width + data.suppliers + ci);

                                    if currentState - 1 >= lb
                                        x_temp((maxStep-1)*width + data.suppliers + ci) = round(currentState - 1);
                                        break;
                                    end;
                                end;
                                num = num + 1;
                            end;
                        end;
                    end;
                end;
            
            case 2 % mutate energy provider partitioning

                % find most expensive slot => select random
                % maxCosts = 0;
                maxStep = randi(data.future_steps);

                % for t=1:data.future_steps
                %
                %     costs = 0;
                %     for g=1:data.grids
                %         for y2=1:data.suppliers
                %             if data.supply_indicator(g,y2) == 1
                %                 costs = costs + q(y2,t) * data.prices(y2,t) * s(y2,t) * d(g, t);
                %             end;
                %         end;
                %     end;
                %
                %     % more expensive slot found
                %     if costs > maxCosts
                %         maxCosts = costs;
                %         maxStep = t;
                %     end;
                % end;

                % choose two unique suppliers
                supplier1 = randi(data.suppliers);
                supplier2 = supplier1;
                while supplier2 == supplier1
                    supplier2 = randi(data.suppliers);
                end;

                % supplier 1 is more expensive than supplier 2
                if data.prices(supplier1,maxStep) < data.prices(supplier2,maxStep)
                    temp = supplier1;
                    supplier1 = supplier2;
                    supplier2 = temp;
                end;

                % set decrease interval
                decrease = 1;
                if q(supplier1,maxStep) < decrease
                    decrease = q(supplier1,maxStep);
                end;

                % set new partitioning
                p1 = round(q(supplier1,maxStep) - decrease);
                p2 = round(q(supplier2,maxStep) + decrease);

                x_temp((maxStep-1)*width + supplier1) = p1;
                x_temp((maxStep-1)*width + supplier2) = p2;

                % update state
                if round(p1) == 0
                    x_temp((maxStep-1)*width + data.suppliers + data.consumers + supplier1) = 0;
                end;

            case 3 % mutate batter charging/providing (future work)
        end;

        % validate the mutated child
        valid=validate(x_temp, constraints.linear_A, constraints.linear_b, constraints.linear_Aeq, constraints.linear_beq, constraints.lower_bounds, constraints.upper_bounds, ConstrFcn, tolCon);

        % increment counter
        valCount = valCount + 1;

        % set mutated child
        mutation = x_temp;
    end;
        
    % ----------------------------------------------------------------

    % no log message
end

