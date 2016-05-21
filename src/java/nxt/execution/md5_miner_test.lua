local tickcount = 1;

function isPrime(n)
	primes={}
	if n<=0 then return false end
	if n<=2 then return true end
	if (n%2==0) then return false end
	for i=3,n/2,2 do
		if (n%i==0) then return false end
	end
	return true
end

function work(input1)
  local_state=input1
  while tickcount<1000 do
    tickcount = tickcount +1;
    local_state = input1+tickcount;
  end
  return local_state%9086;
end

function bounty(input1)
  if(isPrime(input1))
  then
    return true
  else
    return false
  end
end
