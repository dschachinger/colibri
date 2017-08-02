function [Population] = create(GenomeLength, ~, options, ConstrFcn)
% This is the problem-specific popluation creation function for the genetic algorithm.

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
    disp('MATLAB: create start');
    
    % ----------------------------------------------------------------
    
    % define globals
    global constraints;
    global data;
    
    % ----------------------------------------------------------------
      
    %Population = gacreationnonlinearfeasible(GenomeLength, FitnessFcn, options,ConstrFcn);    
    
    % get population properties
    totalPopulation = sum(options.PopulationSize);
    initPopulation = options.InitialPopulation;
    options.InitialPopulation = [];
    numProvided = size(initPopulation,1);
    numCreate = totalPopulation - size(initPopulation,1);
    
    if numCreate <= 0
        return;
    end
    
    % properties of solution
    steps = data.future_steps;
    width = GenomeLength/data.future_steps;
    storages = data.consumers - size(data.zone_parameter_control,3);
    suppliers = data.suppliers;
    controls = data.consumers - storages;
    production_reduction = 10;
    
    Population = Inf(totalPopulation, GenomeLength);
    
    % create individuals for population
    for i=1:numCreate
        
        valid = false;
        x = NaN(1,GenomeLength);
        
        while ~valid
            
            for t=1:steps
                
                % set demand parts for suppliers
                p = zeros(1,suppliers);
                
                % TODO decide reduction on value of nonlinear constraint c
                
                % define percentage for producers
                for s=1:suppliers
                    if data.supply_production(1,s) == 1
                        
                        last = x(1,((t-1)*width)+s);                                                
                        if isnan(last)
                            last = 100 + production_reduction;
                        end;
                        
                        n = last+1-production_reduction-sum(p);
                        
                        if n < 1
                            n = 1;
                        end;
                        
                        p(s) = randi(n)-1;                        
                    end;
                end;
                
                % define percentage for other suppliers
                for s=1:suppliers
                    if data.supply_production(1,s) == 0
                        p(s) = randi(101-sum(p))-1;
                    end;
                end;
                
                % TODO define percentage for storages separate
                
                for s=suppliers:-1:1
                    if data.supply_production(1,s) == 0
                        p(s) = 100-sum(p)+p(s);
                        break;
                    end;                    
                end;
                
                % TODO p = p(randperm(suppliers));
                x(1,((t-1)*width)+1:((t-1)*width)+suppliers) = p;
                
                % set provide state of suppliers
                x(1,((t-1)*width)+suppliers+controls+storages+1:((t-1)*width)+suppliers+controls+storages+suppliers) = (p>0);
                
                % set consume state of storages
                store = 1;
                for s=1:suppliers
                    
                    if (data.supply_storage(1,s) == 1)
                        
                        if (p(s) <= 25) && (rand >= 0.5)
                            x(1,((t-1)*width)+suppliers+controls+storages+s) = 0;
                            x(1,((t-1)*width)+suppliers+controls+store) = 0;
                        end;
                        
                        store = store + 1;
                    end;
                end;
                
                % set states of control devices
                controlFirst = ((t-1)*width)+suppliers+1;
                controlLast = ((t-1)*width)+suppliers+controls;
                
                if t==1
                    
                    % use random states
                    for c=controlFirst:controlLast
                        x(1,c) = randi(constraints.upper_bounds(c)+1)-1+constraints.lower_bounds(c);
                    end;
                    
                else
                    
                    % set random states near the last, within the bounds
                    for c=controlFirst:controlLast
                        
                        last = x(1,c-width);
                        
                        range = constraints.upper_bounds(c) - constraints.lower_bounds(c);
                        
                        min = last - 0.25 * range;
                        max = last + 0.25 * range;
                        
                        if max > constraints.upper_bounds(c)
                            max = constraints.upper_bounds(c);
                        end;
                        
                        if min < constraints.lower_bounds(c)
                            min = constraints.lower_bounds(c);
                        end;
                        
                        x(1,c) = randi(max-min+1)-1+min;
                    end;
                end;
            end;
            Population(i,:) = x;            
            valid=validate(x, constraints.linear_A, constraints.linear_b, constraints.linear_Aeq, constraints.linear_beq, constraints.lower_bounds, constraints.upper_bounds, ConstrFcn, options.TolCon);
            
            % TODO add additional stop condition
        end;
    end;
    
    % add provided population to final population
    if numProvided > 0
        Population(numCreate+1:numCreate+numProvided,:) = initPopulation;
    end;

    % ----------------------------------------------------------------
    
    % no log message
    
end

