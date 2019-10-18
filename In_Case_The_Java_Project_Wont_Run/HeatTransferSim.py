'''
Heat Flow Simulator 
Jared Scott

Assumptions
Cloud cover is neglected
Rate of energy coming form the sun is considered constant
The fluid in the pipes is water.
It is assumed that the pump causes the temperature of the system to stay homogeneous
Specific heat of water: 4.186 joule/gram per degree C
(Source: http://hyperphysics.phy-astr.gsu.edu/hbase/Tables/sphtt.html#c1)
Weight of water 1 kg per liter
Average energy absorbed by solar panels that becomes heat: 47%
Source: http://solarcellcentral.com/limits_page.html)
Average max temperature that an object can achieve in sunlight: 66 C
(Source: https://physics.stackexchange.com/questions/186910/how-hot-can-metal-get-in-sunlight)
Amount of energy coming from the sun: 1360 W (1360 (joules)/ second) per square meter  
(Source: https://earthobservatory.nasa.gov/features/EnergyBalance/page2.php)
'''
inputValid = False
maxTempReached = False

#Validating input
while (not inputValid):
    sizeOfPanel = input("Size Of Panel (Square Meters): ")
    waterVolSystem = input("Volume Of Water In System (Liters): ")
    tempSystemStart = input("Temperature Of System At Start (Degrees Celsius): ")
    simTime = input("Simulation Time (Minutes): ")
    try:
        sizeOfPanel = float(sizeOfPanel)
        massOfWaterInSystem = float(waterVolSystem) * 1000 #Converting from liters to grams
        tempSystemStart = float(tempSystemStart)
        simTime = int(simTime) * 60 #Converting to seconds
        inputValid = True
    except ValueError:
        print("Input invalid\nAll input must be a number")       

#Running the simulation
if (inputValid):
    '''
     I wanted to keep track of many more factors for accuracy. 
     Spent quite a bit of time trying to account for mass flow rates, radiant heat loss, enthalpy etc.
     This became very complicated very quickly so I stuck with an unrealistic simple model.
     Used the equation Q = mc(dT) 
     Q = energy transfer
     m = mass
     c = specific heat
     dT = Change of temperature  
     
     Simple model of heat increase of the system
    '''                 
    print("\n\nRunning Simulation\n")
    energyFromEnv = 0
    for i in range(1,simTime + 1):
        energyFromEnv +=  (1360 * sizeOfPanel * .47) #1360 joules/second per m^2 at 47% efficiency see assumptions
        tempDifSystem = energyFromEnv / (massOfWaterInSystem * 4.186) #4.186 being the specific heat of water see assumptions
        tempSystemEnd = tempSystemStart + tempDifSystem;
        
        if tempSystemEnd > 66 and not maxTempReached:
            maxTempReached = True
            maxTempTime = round((i/60),4)

    if maxTempReached:
        tempSystemEnd = 66 #supposed maximum temperature that an object can get in sunlight
        print("Maximum Temperature Achieved")
        print("Excess Heat Lost To Environment")
        print("\nTime Taken To Reach Maximum Temperature (Minutes): " + str(maxTempTime))
    print("\nThe Energy Transferred To The Solar Panel (Joules): " + str(round(energyFromEnv,3)) + " Joules")
    print("\nTotal Heat Added To The System (Degrees Celsius): " + str(round(tempDifSystem,3)))
    print("\nThe Final Temperature Of The System (Degrees Celsius): " + str(round(tempSystemEnd,3)))